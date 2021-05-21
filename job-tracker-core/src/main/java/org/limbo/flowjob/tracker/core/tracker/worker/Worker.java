/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.tracker.worker;

import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.job.JobContext;
import org.limbo.flowjob.tracker.core.messaging.SendJobResult;
import reactor.core.publisher.Mono;

/**
 * 在Tracker端，作业执行节点的抽象。
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface Worker {

    /**
     * 返回worker节点ID。
     * @return worker节点ID
     */
    String getId();

    /**
     * 获取worker节点最近一次上报的指标信息。
     * @return 本worker节点最近一次上报的指标信息
     */
    WorkerMetric metric();

    /**
     * worker节点心跳检测。
     * @return 返回worker节点的指标信息。
     */
    Mono<WorkerMetric> ping();

    /**
     * 发送一个作业到worker执行。当worker接受此job后，将触发返回的{@link Mono}
     * @param context 作业执行上下文
     * @return worker接受job后触发
     */
    Mono<SendJobResult> sendJobContext(JobContext context) throws JobWorkerException;

    /**
     * 当worker被移除时触发。触发worker移除有两种方式：1. worker心跳超时；2. worker主动申请下线
     * @return worker被移除时触发的Mono
     */
    Mono<Void> onUnregistered();

}
