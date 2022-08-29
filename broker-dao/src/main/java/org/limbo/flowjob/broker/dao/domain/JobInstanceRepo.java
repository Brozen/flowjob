/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Override
    public String save(JobInstance jobInstance) {
        JobInstanceEntity entity = toEntity(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public JobInstance get(String jobInstanceId) {
        return jobInstanceEntityRepo.findById(Long.valueOf(jobInstanceId)).map(entity -> {

            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId()).get();
            DAG<JobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobs());
            return DomainConverter.toJobInstance(entity, dag);

        }).orElse(null);
    }

    private JobInstanceEntity toEntity(JobInstance jobInstance) {
        JobInstanceEntity jobInstanceEntity = new JobInstanceEntity();
        jobInstanceEntity.setPlanInstanceId(Long.valueOf(jobInstance.getPlanInstanceId()));
        jobInstanceEntity.setPlanInfoId(Long.valueOf(jobInstance.getPlanVersion()));
        jobInstanceEntity.setJobId(jobInstance.getJobId());
        jobInstanceEntity.setStatus(jobInstance.getStatus().status);
        jobInstanceEntity.setAttributes(jobInstance.getAttributes().toString());
        jobInstanceEntity.setStartAt(jobInstance.getStartAt());
        jobInstanceEntity.setEndAt(jobInstance.getEndAt());
        jobInstanceEntity.setId(Long.valueOf(jobInstance.getJobInstanceId()));
        return jobInstanceEntity;
    }

}
