/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerResourceParam;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.executor.JobExecutor;
import org.limbo.flowjob.worker.core.executor.JobExecutorRunner;
import org.limbo.flowjob.worker.core.executor.JobManager;
import org.limbo.flowjob.worker.core.remote.AbstractRemoteClient;
import org.limbo.flowjob.worker.core.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作节点实例
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class Worker {
    /**
     * id
     */
    private String id;

    private String host;

    private int port;
    /**
     * 工作节点资源
     */
    private WorkerResource resource;
    /**
     * 执行器名称 - 执行器 映射关系
     */
    private final Map<String, JobExecutor> executors;
    /**
     * job 管理中心
     */
    private final JobManager jobManager;
    /**
     * 远程调用
     */
    private AbstractRemoteClient remoteClient;
    /**
     * 是否已经启动
     */
    private volatile boolean started = false;

    public Worker(String host, int port, int queueSize, List<JobExecutor> executors, AbstractRemoteClient remoteClient) throws Exception {
        Verifies.notEmpty(executors, "empty executors");
        Verifies.notNull(remoteClient, "remote client can't be null");

        this.id = UUIDUtils.randomID();
        this.host = StringUtils.isBlank(host) ? NetUtils.getLocalIp() : host;
        this.port = port;
        this.resource = WorkerResource.create(queueSize);
        this.executors = new ConcurrentHashMap<>();
        this.jobManager = new JobManager();
        this.remoteClient = remoteClient;

        for (JobExecutor executor : executors) {
            Verifies.notBlank(executor.getName(), "has blank executor name");
            this.executors.put(executor.getName(), executor);
        }
    }

    /**
     * 启动
     * @param host server地址
     * @param port server端口
     * @param heartbeatPeriod 心跳间隔
     */
    public synchronized void start(String host, int port, int heartbeatPeriod) {
        // 重复检测
        if (started) {
            return;
        }

        // 建立连接
        remoteClient.start(host, port);

        // 注册
        register();

        // 心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                heartbeat();
            }
        }, 200, heartbeatPeriod);

        // 启动完成
        started = true;
    }

    /**
     * 向tracker注册
     */
    public void register() {
        // 注册数据
        WorkerResourceParam resourceDto = new WorkerResourceParam();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerRegisterParam registerOptionDto = new WorkerRegisterParam();
//        registerOptionDto.setId(id);
        registerOptionDto.setHost(host);
        registerOptionDto.setPort(port);
        // 执行器
        List<WorkerExecutorRegisterParam> workerExecutors = new ArrayList<>();
        for (JobExecutor executor : executors.values()) {
            WorkerExecutorRegisterParam workerExecutorRegisterDto = new WorkerExecutorRegisterParam();
            workerExecutorRegisterDto.setName(executor.getName());
            workerExecutorRegisterDto.setDescription(executor.getDescription());
            workerExecutorRegisterDto.setType(executor.getType());
            workerExecutors.add(workerExecutorRegisterDto);
        }
        registerOptionDto.setExecutors(workerExecutors);
        registerOptionDto.setAvailableResource(resourceDto);
        registerOptionDto.setProtocol(remoteClient.getProtocol());

        ResponseDTO<WorkerRegisterDTO> register = remoteClient.register(registerOptionDto);
        if (!register.isOk()) {
            // todo 注册失败
            log.error(JacksonUtils.toJSONString(register.getData()));
        }

        // todo 注册各个节点到client

        log.info("register success !");
    }

    /**
     * 发送心跳
     */
    public void heartbeat() {
        // 数据
        WorkerResourceParam resourceDto = new WorkerResourceParam();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueSize());

        WorkerHeartbeatParam heartbeatOptionDto = new WorkerHeartbeatParam();
        heartbeatOptionDto.setWorkerId(id);
        heartbeatOptionDto.setAvailableResource(resourceDto);

        ResponseDTO<Void> heartbeat = remoteClient.heartbeat(heartbeatOptionDto);
        // todo 心跳失败
        if (log.isDebugEnabled()) {
            log.debug("send heartbeat success");
        }

        // todo 注册各个节点到client
    }

    /**
     * 提交任务
     */
    public synchronized void receive(Task job) {
        Verifies.verify(executors.containsKey(job.getExecutorName()), "worker doesn't " + job.getExecutorName() + " executor");
        Verifies.verify(jobManager.size() < resource.getAvailableQueueSize(), "worker's queue is full");

        log.info("receive job " + job.getId());

        // todo 是否超过cpu/ram/queue 失败

        JobExecutor jobExecutor = executors.get(job.getExecutorName());

        JobExecutorRunner runner = new JobExecutorRunner(jobManager, jobExecutor, remoteClient);

        runner.run(job);
    }

    /**
     * 查询任务状态
     */
    public void jobState(String jobId) {
        if (jobManager.hasJob(jobId)) {

        }
    }

    /**
     * 扩缩容队列
     */
    public void resize(int queueSize) {
        resource.resize(queueSize);
    }

}
