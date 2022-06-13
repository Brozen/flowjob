package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanRecordPoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanRecordMapper planRecordMapper;

    @Autowired
    private PlanRecordPoConverter converter;


    @Override
    public void add(PlanInstance record) {
        PlanInstanceEntity po = converter.convert(record);
        planRecordMapper.insert(po);
    }


    @Override
    public PlanInstance get(PlanInstance.ID planRecordId) {
        PlanInstanceEntity po = planRecordMapper.selectOne(Wrappers.<PlanInstanceEntity>lambdaQuery()
                .eq(PlanInstanceEntity::getPlanInstanceId, planRecordId.planId + planRecordId.planRecordId)
        );
        return converter.reverse().convert(po);
    }


    @Override
    public PlanInstance.ID createId(String planId) {
        Long recentlyIdForUpdate = planRecordMapper.getRecentlyIdForUpdate(planId);
        long planRecordId = recentlyIdForUpdate == null ? 1L : recentlyIdForUpdate + 1;
        return new PlanInstance.ID(planId, planRecordId);
    }


    @Override
    public void end(PlanInstance.ID planRecordId, PlanScheduleStatus state) {
        planRecordMapper.update(null, Wrappers.<PlanInstanceEntity>lambdaUpdate()
                .set(PlanInstanceEntity::getState, state.status)
                .set(PlanInstanceEntity::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstanceEntity::getPlanInstanceId, planRecordId.planId + planRecordId.planRecordId)
                .eq(PlanInstanceEntity::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }

}
