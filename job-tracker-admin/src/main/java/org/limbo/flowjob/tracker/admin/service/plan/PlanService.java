package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.dto.job.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.ExecutorOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanReplaceDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobDAG;
import org.limbo.flowjob.tracker.core.plan.*;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private TrackerNode trackerNode;

    @Autowired
    private PlanInfoBuilderFactory planInfoBuilderFactory;

    /**
     * 新增计划 只是个落库操作
     */
    public String add(PlanAddDto dto) {
        Plan plan = convertToPlan(dto);
        PlanInfo planInfo = convertToPlanInfo(plan, dto);
        return planRepository.addPlan(plan, planInfo);
    }


    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    public void replace(String planId, PlanReplaceDto dto) {
        // 获取当前的plan数据
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan not exist");

        // 更新版本数据
        PlanInfo planInfo = convertToPlanInfo(plan, dto);
        plan.addNewVersion(planInfo);

        // 需要修改plan重新调度
        if (trackerNode.jobTracker().isScheduling(planId)) {
            trackerNode.jobTracker().unschedule(planId);
            trackerNode.jobTracker().schedule(plan.getCurrentVersionInfo());
        }
    }


    /**
     * 启动计划 开始调度 todo 并发
     */
    public void start(String planId) {
        // 校验，计划存在且已启用
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan is not exist");

        // 已经启动不重复处理
        if (plan.isEnabled()) {
            return;
        }

        // 获取当前版本的计划信息，并校验Jobs
        PlanInfo planInfo = plan.getCurrentVersionInfo();
        JobDAG dag = planInfo.getDag();
        Verifies.notNull(dag, "jobs not exist");
        Verifies.notEmpty(dag.getEarliestJobs(), "jobs is empty");

        // 更新状态
        if (plan.enable()) {
            // 调度 plan
            trackerNode.jobTracker().schedule(planInfo);
        }
    }


    /**
     * 取消计划 停止调度
     */
    public void stop(String planId) {
        // 查询计划
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan is not exist");

        // 已经停止不重复处理
        if (!plan.isEnabled()) {
            return;
        }

        // 停用计划
        if (plan.disable()) {
            // 停止调度 plan
            trackerNode.jobTracker().unschedule(planId);
        }
    }


    /**
     * 将新增执行计划dto转换为Plan领域对象
     * FIXME 是否考虑抽取converter
     * @param dto 新增执行计划dto
     * @return Plan领域对象
     */
    private Plan convertToPlan(PlanAddDto dto) {
        // Plan dto 转 do
        Plan plan = new Plan();
        plan.setPlanId(dto.getPlanId());
        plan.setEnabled(false);

        return plan;
    }


    /**
     * 将新增执行计划dto转换为PlanInfo领域对象
     * @param plan 执行计划
     * @param dto 新增执行计划dto
     * @return PlanInfo领域对象
     */
    private PlanInfo convertToPlanInfo(Plan plan, PlanAddDto dto) {
        return planInfoBuilderFactory.builder()
                .planId(plan.getPlanId())
                .description(dto.getDescription())
                .scheduleOption(convertToVo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }


    /**
     * 将修改执行计划dto转换为PlanInfo领域对象
     * @param plan 执行计划
     * @param dto 修改执行计划dto
     * @return PlanInfo领域对象
     */
    private PlanInfo convertToPlanInfo(Plan plan, PlanReplaceDto dto) {
        return planInfoBuilderFactory.builder()
                .planId(plan.getPlanId())
                .description(dto.getDescription())
                .scheduleOption(convertToVo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }


    private DispatchOption convertToDo(DispatchOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new DispatchOption(dto.getLoadBalanceType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    private ExecutorOption convertToDo(ExecutorOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ExecutorOption(dto.getName(), dto.getType());
    }

    private ScheduleOption convertToVo(ScheduleOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron(), dto.getRetry());
    }

    private List<Job> convertToDo(List<JobDto> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }
        // 封装对象
        for (JobDto dto : dtos) {
            list.add(convertToDo(dto));
        }
        return list;
    }

    private Job convertToDo(JobDto dto) {
        Job job = new Job();
        job.setJobId(dto.getJobId());
        job.setDescription(dto.getDescription());
        job.setChildrenIds(dto.getChildrenIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        job.setExecutorOption(convertToDo(dto.getExecutorOption()));
        return job;
    }

}
