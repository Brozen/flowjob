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

package org.limbo.flowjob.worker.core.remote;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;

/**
 * worker与tracker建立连接 并实现 心跳 和 故障转移
 *
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public abstract class AbstractRemoteClient {

    // todo 失败重试
    protected void request() {

    }

    /**
     * 与tracker建立连接
     */
    public abstract void start(String host, int port);

    /**
     * 发送心跳给tracker
     */
    public abstract ResponseDTO<Void> heartbeat(WorkerHeartbeatParam param);

    /**
     * 注册当前的worker
     */
    public abstract ResponseDTO<WorkerRegisterDTO> register(WorkerRegisterParam param);

    /**
     * 任务执行完成 将结果反馈给tracker
     */
    public abstract void taskExecuted(TaskExecuteFeedbackParam param);

    /**
     * 协议类型
     */
    public abstract WorkerProtocol getProtocol();

}
