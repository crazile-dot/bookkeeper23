/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bookkeeper.stream.storage.impl.metadata;

import java.util.concurrent.CompletableFuture;
/*import org.apache.bookkeeper.stream.proto.storage.CreateNamespaceRequest;
import org.apache.bookkeeper.stream.proto.storage.CreateNamespaceResponse;
import org.apache.bookkeeper.stream.proto.storage.StatusCode;*/
import org.apache.bookkeeper.stream.storage.impl.AsyncOperationProcessor;
//import org.graalvm.compiler.hotspot.phases.OnStackReplacementPhase;

/**
 * The operation process for creating namespace.
 */
class CreateNamespaceProcessor
{    //extends AsyncOperationProcessor<CreateNamespaceRequest, CreateNamespaceResponse, RootRangeStoreImpl> {

    public static CreateNamespaceProcessor of() {
        return INSTANCE;
    }

    private static final CreateNamespaceProcessor INSTANCE = new CreateNamespaceProcessor();

    private CreateNamespaceProcessor() {
    }

    //@Override
    protected Object verifyRequest(RootRangeStoreImpl state,
                                       Object request) {
        //return state.verifyCreateNamespaceRequest(request);
        return null;
    }

    //@Override
    protected Object failRequest(Object code) {
        /*return CreateNamespaceResponse.newBuilder()
            .setCode(code)
            .build();*/
        return null;
    }

    //@Override
    protected CompletableFuture<Object> doProcessRequest(RootRangeStoreImpl state,
                                                                          Object request) {
        //return state.doProcessCreateNamespaceRequest(request);
        return null;
    }
}
