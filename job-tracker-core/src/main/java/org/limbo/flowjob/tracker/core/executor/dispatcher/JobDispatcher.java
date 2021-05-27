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

package org.limbo.flowjob.tracker.core.executor.dispatcher;

import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * 作业分发器，封装了作业分发时的worker选择规则{@link JobDispatchType}：
 * <ul>
 *     <li>{@link JobDispatchType#ROUND_ROBIN}</li>
 *     <li>{@link JobDispatchType#RANDOM}</li>
 *     <li>{@link JobDispatchType#APPOINT}</li>
 *     <li>{@link JobDispatchType#LEAST_FREQUENTLY_USED}</li>
 *     <li>{@link JobDispatchType#LEAST_RECENTLY_USED}</li>
 *     <li>{@link JobDispatchType#CONSISTENT_HASH}</li>
 * </ul>
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobDispatcher {

    /**
     * 选择作业上下文应当下发给的worker。
     * @param context 待下发的作业上下文
     * @param workers 待下发上下文可用的worker
     * @param executor 作业执行回调
     */
    void dispatch(JobContext context, Collection<Worker> workers, BiConsumer<JobContext, Worker> executor);

}
