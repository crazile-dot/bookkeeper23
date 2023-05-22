/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bookkeeper.clients.impl.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.clients.impl.channel.StorageServerChannel;
import org.apache.bookkeeper.clients.impl.channel.StorageServerChannelManager;
import org.apache.bookkeeper.clients.impl.container.StorageContainerChannel;
import org.apache.bookkeeper.clients.impl.container.StorageContainerChannelManager;
import org.apache.bookkeeper.clients.impl.internal.api.LocationClient;
import org.apache.bookkeeper.clients.impl.internal.api.MetaRangeClient;
import org.apache.bookkeeper.clients.impl.internal.api.RootRangeClient;
import org.apache.bookkeeper.clients.impl.internal.api.StorageServerClientManager;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
import org.apache.bookkeeper.common.util.AbstractAutoAsyncCloseable;
//import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.common.util.SharedResourceManager;
import org.apache.bookkeeper.common.util.SharedResourceManager.Resource;
//import org.apache.bookkeeper.stream.proto.StreamProperties;
//import org.apache.bookkeeper.stream.proto.common.Endpoint;

/**
 * A gRPC based {@link StorageServerClientManager} implementation.
 */
@Slf4j
public abstract class StorageServerClientManagerImpl
    extends AbstractAutoAsyncCloseable
    implements StorageServerClientManager {

    private final Resource<Object> schedulerResource;
    private final Object scheduler;
    private final StorageServerChannelManager channelManager = null;
    private final StorageContainerChannelManager scChannelManager = null;

    // clients
    private final LocationClient locationClient = null;
    private final RootRangeClient rootRangeClient = null;
    private final StreamMetadataCache streamMetadataCache = null;
    private final ConcurrentMap<Long, MetaRangeClientImpl> metaRangeClients;

    public StorageServerClientManagerImpl(Object settings,
                                          Resource<Object> schedulerResource) {
        this(
            null,
            schedulerResource,
            null);
    }

    public StorageServerClientManagerImpl(Object settings,
                                          Resource<Object> schedulerResource,
                                          Function<Object, StorageServerChannel> channelFactory) {
        this.schedulerResource = schedulerResource;
        this.scheduler = SharedResourceManager.shared().get(schedulerResource);
        //this.locationClient = new LocationClientImpl(settings, null);
        //this.channelManager = new StorageServerChannelManager(channelFactory);
        /*this.scChannelManager = new StorageContainerChannelManager(
            this.channelManager,
            this.locationClient,
            null);*/
        /*this.rootRangeClient = new RootRangeClientImplWithRetries(
            new RootRangeClientImpl(
                null,
                scChannelManager),
            settings.backoffPolicy(),
            null
        );*/
       // this.streamMetadataCache = new StreamMetadataCache(rootRangeClient);
        this.metaRangeClients = Maps.newConcurrentMap();
    }

    @VisibleForTesting
    StorageContainerChannelManager getStorageContainerChannelManager() {
        return scChannelManager;
    }

    @Override
    public StorageContainerChannel getStorageContainerChannel(long scId) {
        return scChannelManager.getOrCreate(scId);
    }

    @Override
    public LocationClient getLocationClient() {
        return this.locationClient;
    }

    @Override
    public RootRangeClient getRootRangeClient() {
        return this.rootRangeClient;
    }

    //@Override
    public MetaRangeClientImpl openMetaRangeClient(Object streamProps) {
       // MetaRangeClientImpl client = metaRangeClients.get(streamProps.getStreamId());
        /*if (null != client) {
            return client;
        }*/
        MetaRangeClientImpl newClient = null;
        MetaRangeClientImpl oldClient = null;
        if (null != oldClient) {
            return oldClient;
        } else {
            streamMetadataCache.putStreamProperties(000, null);
            return newClient;
        }
    }


    @Override
    public CompletableFuture<MetaRangeClient> openMetaRangeClient(long streamId) {
        MetaRangeClientImpl client = metaRangeClients.get(streamId);
        if (null != client) {
            return FutureUtils.value(client);
        }
        return getStreamProperties(streamId).thenApply(props -> openMetaRangeClient(props));
    }

    @Override
    protected void closeAsyncOnce(CompletableFuture<Void> closeFuture) {
        locationClient.close();
        channelManager.close();
        /*scheduler.submit(() -> {
            SharedResourceManager.shared().release(schedulerResource, scheduler);
            closeFuture.complete(null);
        });*/
    }
}
