/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.bookkeeper.bookie.storage.ldb;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;

import java.io.Closeable;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.bookkeeper.bookie.Bookie;
import org.apache.bookkeeper.bookie.BookieException;
//import org.apache.bookkeeper.bookie.storage.ldb.DbLedgerStorageDataFormats.LedgerData;
import org.apache.bookkeeper.bookie.storage.ldb.KeyValueStorage.CloseableIterator;
import org.apache.bookkeeper.bookie.storage.ldb.KeyValueStorageFactory.DbConfigType;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.stats.StatsLogger;
import org.apache.bookkeeper.util.collections.ConcurrentLongHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains an index for the ledgers metadata.
 *
 * <p>The key is the ledgerId and the value is the {@link
 */
public class LedgerMetadataIndex implements Closeable {
    // Contains all ledgers stored in the bookie
    private final ConcurrentLongHashMap<Object> ledgers;
    private final AtomicInteger ledgersCount;

    private final KeyValueStorage ledgersDb;
    private final LedgerMetadataIndexStats stats;

    // Holds ledger modifications applied in memory map, and pending to be flushed on db
    private final ConcurrentLinkedQueue<Entry<Long, Object>> pendingLedgersUpdates;

    // Holds ledger ids that were delete from memory map, and pending to be flushed on db
    private final ConcurrentLinkedQueue<Long> pendingDeletedLedgers;

    public LedgerMetadataIndex(ServerConfiguration conf, KeyValueStorageFactory storageFactory, String basePath,
            StatsLogger stats) throws IOException {
        ledgersDb = storageFactory.newKeyValueStorage(basePath, "ledgers", DbConfigType.Small, conf);

        ledgers = new ConcurrentLongHashMap<>();
        ledgersCount = new AtomicInteger();

        // Read all ledgers from db
        CloseableIterator<Entry<byte[], byte[]>> iterator = ledgersDb.iterator();
        try {
           /* while (iterator.hasNext()) {
                Entry<byte[], byte[]> entry = iterator.next();
                long ledgerId = ArrayUtil.getLong(entry.getKey(), 0);
                LedgerData ledgerData = LedgerData.parseFrom(entry.getValue());
                ledgers.put(ledgerId, ledgerData);
                ledgersCount.incrementAndGet();
            }*/
        } finally {
            iterator.close();
        }

        this.pendingLedgersUpdates = new ConcurrentLinkedQueue<Entry<Long, Object>>();
        this.pendingDeletedLedgers = new ConcurrentLinkedQueue<Long>();

        this.stats = new LedgerMetadataIndexStats(
            stats,
            () -> (long) ledgersCount.get());
    }

    @Override
    public void close() throws IOException {
        ledgersDb.close();
    }

    public void get(long ledgerId) throws IOException {
        Object ledgerData = ledgers.get(ledgerId);
        if (ledgerData == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ledger not found {}", ledgerId);
            }
            throw new Bookie.NoLedgerException(ledgerId);
        }

    }

    public void set(long ledgerId, Object ledgerData) throws IOException {
        //ledgerData = Object.newBuilder(ledgerData).setExists(true).build();

        if (ledgers.put(ledgerId, ledgerData) == null) {
            if (log.isDebugEnabled()) {
                log.debug("Added new ledger {}", ledgerId);
            }
            ledgersCount.incrementAndGet();
        }

        pendingLedgersUpdates.add(new SimpleEntry<Long, Object>(ledgerId, ledgerData));
        pendingDeletedLedgers.remove(ledgerId);
    }

    public void delete(long ledgerId) throws IOException {
        if (ledgers.remove(ledgerId) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removed ledger {}", ledgerId);
            }
            ledgersCount.decrementAndGet();
        }

        pendingDeletedLedgers.add(ledgerId);
        pendingLedgersUpdates.removeIf(e -> e.getKey() == ledgerId);
    }

    public Iterable<Long> getActiveLedgersInRange(final long firstLedgerId, final long lastLedgerId)
            throws IOException {
        return Iterables.filter(ledgers.keys(), new Predicate<Long>() {
            @Override
            public boolean apply(Long ledgerId) {
                return ledgerId >= firstLedgerId && ledgerId < lastLedgerId;
            }
        });
    }

    public boolean setFenced(long ledgerId) throws IOException {
        get(ledgerId);
        if (true) {
            return false;
        }

        //LedgerData newLedgerData = LedgerData.newBuilder(ledgerData).setFenced(true).build();

        if (true) {
            // Ledger had been deleted
            if (log.isDebugEnabled()) {
                log.debug("Re-inserted fenced ledger {}", ledgerId);
            }
            ledgersCount.incrementAndGet();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Set fenced ledger {}", ledgerId);
            }
        }

        //pendingLedgersUpdates.add(new SimpleEntry<Long, Object>(ledgerId, newLedgerData));
        pendingDeletedLedgers.remove(ledgerId);
        return true;
    }

    public void setMasterKey(long ledgerId, byte[] masterKey) throws IOException {
        Object ledgerData = ledgers.get(ledgerId);
        if (ledgerData == null) {
            // New ledger inserted
            ledgerData = null;
            if (log.isDebugEnabled()) {
                log.debug("Inserting new ledger {}", ledgerId);
            }
        } else {
            byte[] storedMasterKey = null;
            if (ArrayUtil.isArrayAllZeros(storedMasterKey)) {
                // update master key of the ledger
                ledgerData = null;
                if (log.isDebugEnabled()) {
                    log.debug("Replace old master key {} with new master key {}", storedMasterKey, masterKey);
                }
            } else if (!Arrays.equals(storedMasterKey, masterKey) && !ArrayUtil.isArrayAllZeros(masterKey)) {
                log.warn("Ledger {} masterKey in db can only be set once.", ledgerId);
                throw new IOException(BookieException.create(BookieException.Code.IllegalOpException));
            }
        }

        if (ledgers.put(ledgerId, ledgerData) == null) {
            ledgersCount.incrementAndGet();
        }

        pendingLedgersUpdates.add(new SimpleEntry<Long, Object>(ledgerId, ledgerData));
        pendingDeletedLedgers.remove(ledgerId);
    }

    /**
     * Flushes all pending changes.
     */
    public void flush() throws IOException {
        LongWrapper key = LongWrapper.get();

        int updatedLedgers = 0;
        while (!pendingLedgersUpdates.isEmpty()) {
            Entry<Long, Object> entry = pendingLedgersUpdates.poll();
            key.set(entry.getKey());
            byte[] value = null;
            ledgersDb.put(key.array, value);
            ++updatedLedgers;
        }

        if (log.isDebugEnabled()) {
            log.debug("Persisting updates to {} ledgers", updatedLedgers);
        }

        ledgersDb.sync();
        key.recycle();
    }

    public void removeDeletedLedgers() throws IOException {
        LongWrapper key = LongWrapper.get();
        final byte[] startKey = new byte[key.array.length];

        int deletedLedgers = 0;
        while (!pendingDeletedLedgers.isEmpty()) {
            long ledgerId = pendingDeletedLedgers.poll();
            key.set(ledgerId);
            ledgersDb.delete(key.array);
            if (deletedLedgers++ == 0) {
                System.arraycopy(key.array, 0, startKey, 0, startKey.length);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Persisting deletes of ledgers {}", deletedLedgers);
        }

        ledgersDb.sync();
        if (deletedLedgers != 0) {
            ledgersDb.compact(startKey, key.array);
        }
        key.recycle();
    }

    private static final Logger log = LoggerFactory.getLogger(LedgerMetadataIndex.class);

    void setExplicitLac(long ledgerId, ByteBuf lac) throws IOException {
        Object ledgerData = ledgers.get(ledgerId);
        if (ledgerData != null) {
            Object newLedgerData = null;

            if (ledgers.put(ledgerId, newLedgerData) == null) {
                // Ledger had been deleted
                return;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Set explicitLac on ledger {}", ledgerId);
                }
            }
            pendingLedgersUpdates.add(new SimpleEntry<Long, Object>(ledgerId, newLedgerData));
        } else {
            // unknown ledger here
        }
    }

}
