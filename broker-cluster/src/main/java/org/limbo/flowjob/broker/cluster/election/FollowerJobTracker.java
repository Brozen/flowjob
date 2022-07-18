///*
// * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *   	http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//package org.limbo.flowjob.broker.cluster.election;
//
//import com.alipay.sofa.jraft.util.Endpoint;
//import org.limbo.flowjob.broker.api.dto.ResponseDTO;
//import org.limbo.flowjob.broker.core.schedule.Schedulable;
//import org.limbo.flowjob.broker.core.node.JobTracker;
//import org.limbo.flowjob.broker.cluster.election.rpc.RpcCaller;
//import org.limbo.flowjob.broker.cluster.election.rpc.request.IsSchedulingRequest;
//import org.limbo.flowjob.broker.cluster.election.rpc.request.ScheduleRequest;
//import org.limbo.flowjob.broker.cluster.election.rpc.request.UnscheduleRequest;
//
///**
// * 转发请求 远程调用 leader
// *
// * @author Devil
// * @since 2021/8/9
// */
//public class FollowerJobTracker implements JobTracker {
//
//    private Endpoint endpoint;
//
//    private RpcCaller rpcCaller;
//
//    public FollowerJobTracker(Endpoint endpoint, RpcCaller rpcCaller) {
//        this.endpoint = endpoint;
//        this.rpcCaller = rpcCaller;
//    }
//
//    @Override
//    public void schedule(Schedulable schedulable) {
//        ScheduleRequest request = new ScheduleRequest();
//        request.setSchedulable(schedulable);
//
//        // TODO 异常处理
//        ResponseDTO<Void> response = rpcCaller.invokeSync(endpoint, request);
//    }
//
//    @Override
//    public void unschedule(String id) {
//        UnscheduleRequest request = new UnscheduleRequest();
//        request.setId(id);
//
//        // TODO 异常处理
//        ResponseDTO<Void> response = rpcCaller.invokeSync(endpoint, request);
//    }
//
//    @Override
//    public boolean isScheduling(String id) {
//        IsSchedulingRequest request = new IsSchedulingRequest();
//        request.setId(id);
//
//        // TODO 异常处理
//        ResponseDTO<Boolean> response = rpcCaller.invokeSync(endpoint, request);
//        return response.getData();
//    }
//}
