/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mgw.filterchain.gRPC.server;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.envoyproxy.envoy.service.auth.v2.AuthorizationGrpc;
import io.envoyproxy.envoy.service.auth.v2.CheckRequest;
import io.envoyproxy.envoy.service.auth.v2.CheckResponse;
import io.envoyproxy.envoy.service.auth.v2.OkHttpResponse;
import io.envoyproxy.envoy.service.auth.v2.DeniedHttpResponse;

import io.grpc.stub.StreamObserver;

public class ExtAuthService extends AuthorizationGrpc.AuthorizationImplBase {

    @Override
    public void check (CheckRequest request, StreamObserver<CheckResponse> responseObserver) {

        System.out.println("++++++++++hit+++++++++++++++");

        System.out.println(request);

        // use a builder to construct a new Protobuffer object
        // jwt authentication should happens here
        //Status status = Status.OK;
        CheckResponse response = CheckResponse.newBuilder()
                .setStatus(Status.newBuilder().setCode(Code.OK_VALUE).build())
                .setOkResponse(OkHttpResponse.newBuilder().build())
                .build();
        

        CheckResponse response1 = CheckResponse.newBuilder()
                .setStatus(Status.newBuilder().setCode(Code.UNAUTHENTICATED_VALUE).build())
                .setDeniedResponse(DeniedHttpResponse.newBuilder().build())
                .build();
        System.out.println("this is respo"+ response1);
        // Use responseObserver to send a single response back
        responseObserver.onNext(response);

        // When you are done, you must call onCompleted.
        responseObserver.onCompleted();
    }
}
