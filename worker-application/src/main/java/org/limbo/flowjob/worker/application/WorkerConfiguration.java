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

package org.limbo.flowjob.worker.application;

import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.executor.TaskExecutor;
import org.limbo.flowjob.worker.core.executor.ShellTaskExecutor;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Configuration
public class WorkerConfiguration {

    @Autowired
    private WorkerProperties workerProperties;

    @Bean
    public Worker worker() throws Exception {
        List<TaskExecutor> executors = new ArrayList<>();
        executors.add(new ShellTaskExecutor());
        executors.add(new HelloExecutor());

        BrokerRpc rpc = null;
        URL baseUrl = new URL("http", workerProperties.getLocalHost(), workerProperties.getLocalPort(), "");
        Worker worker = new Worker("", workerProperties.getQueueSize(), executors, rpc);
        worker.start(baseUrl, Duration.ofSeconds(5));
        return worker;
    }

}
