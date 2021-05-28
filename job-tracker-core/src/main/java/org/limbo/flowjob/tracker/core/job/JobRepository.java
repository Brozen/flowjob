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

package org.limbo.flowjob.tracker.core.job;

import reactor.core.publisher.Flux;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface JobRepository {

    /**
     * 根据id查询作业
     * @param jobId jobId
     * @return 作业
     */
    JobDO getJob(String jobId);

    /**
     * 添加一个作业
     * @param job 作业数据
     */
    void addJob(JobDO job);

    /**
     * 查询所有有效作业，只有有效作业才可被调度。
     * @return 所有有效作业
     */
    Flux<JobDO> schedulableJobs();

}
