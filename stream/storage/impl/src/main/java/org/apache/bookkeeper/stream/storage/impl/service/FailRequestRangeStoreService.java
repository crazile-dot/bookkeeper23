/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bookkeeper.stream.storage.impl.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.CompletableFuture;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
/*import org.apache.bookkeeper.common.util.OrderedScheduler;
import org.apache.bookkeeper.stream.proto.kv.rpc.DeleteRangeRequest;
import org.apache.bookkeeper.stream.proto.kv.rpc.DeleteRangeResponse;
import org.apache.bookkeeper.stream.proto.kv.rpc.IncrementRequest;
import org.apache.bookkeeper.stream.proto.kv.rpc.IncrementResponse;
import org.apache.bookkeeper.stream.proto.kv.rpc.PutRequest;
import org.apache.bookkeeper.stream.proto.kv.rpc.PutResponse;
import org.apache.bookkeeper.stream.proto.kv.rpc.RangeRequest;
import org.apache.bookkeeper.stream.proto.kv.rpc.RangeResponse;
import org.apache.bookkeeper.stream.proto.kv.rpc.TxnRequest;
import org.apache.bookkeeper.stream.proto.kv.rpc.TxnResponse;
import org.apache.bookkeeper.stream.proto.storage.CreateNamespaceRequest;
import org.apache.bookkeeper.stream.proto.storage.CreateNamespaceResponse;
import org.apache.bookkeeper.stream.proto.storage.CreateStreamRequest;
import org.apache.bookkeeper.stream.proto.storage.CreateStreamResponse;
import org.apache.bookkeeper.stream.proto.storage.DeleteNamespaceRequest;
import org.apache.bookkeeper.stream.proto.storage.DeleteNamespaceResponse;
import org.apache.bookkeeper.stream.proto.storage.DeleteStreamRequest;
import org.apache.bookkeeper.stream.proto.storage.DeleteStreamResponse;
import org.apache.bookkeeper.stream.proto.storage.GetActiveRangesRequest;
import org.apache.bookkeeper.stream.proto.storage.GetActiveRangesResponse;
import org.apache.bookkeeper.stream.proto.storage.GetNamespaceRequest;
import org.apache.bookkeeper.stream.proto.storage.GetNamespaceResponse;
import org.apache.bookkeeper.stream.proto.storage.GetStreamRequest;
import org.apache.bookkeeper.stream.proto.storage.GetStreamResponse;*/
import org.apache.bookkeeper.stream.storage.api.metadata.RangeStoreService;

/**
 * It is a single-ton implementation that fails all requests.
 */
abstract class FailRequestRangeStoreService implements RangeStoreService {

    static RangeStoreService of(Object scheduler) {
        //return new FailRequestRangeStoreService(scheduler);
        return null;
    }

    private final Object scheduler;

    private FailRequestRangeStoreService(Object scheduler) {
        this.scheduler = scheduler;
    }

    private <T> CompletableFuture<T> failWrongGroupRequest() {
        CompletableFuture<T> future = FutureUtils.createFuture();
        /*scheduler.execute(() ->
            future.completeExceptionally(new StatusRuntimeException(Status.NOT_FOUND)));*/
        return future;
    }

    @Override
    public CompletableFuture<Void> start() {
        return FutureUtils.Void();
    }

    @Override
    public CompletableFuture<Void> stop() {
        return FutureUtils.Void();
    }

    //
    // Namespace API
    //

   // @Override
    public CompletableFuture<Object> createNamespace(Object request) {
        return failWrongGroupRequest();
    }

   // @Override
    public CompletableFuture<Object> deleteNamespace(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> getNamespace(Object request) {
        return failWrongGroupRequest();
    }

    //
    // Stream API
    //

    //@Override
    public CompletableFuture<Object> createStream(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> deleteStream(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> getStream(Object request) {
        return failWrongGroupRequest();
    }

    //
    // Stream Meta Range API.
    //

    //@Override
    public CompletableFuture<Object> getActiveRanges(Object request) {
        return failWrongGroupRequest();
    }

    //
    // Table API
    //


    //@Override
    public CompletableFuture<Object> range(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> put(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> delete(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> txn(Object request) {
        return failWrongGroupRequest();
    }

    //@Override
    public CompletableFuture<Object> incr(Object request) {
        return failWrongGroupRequest();
    }
}
