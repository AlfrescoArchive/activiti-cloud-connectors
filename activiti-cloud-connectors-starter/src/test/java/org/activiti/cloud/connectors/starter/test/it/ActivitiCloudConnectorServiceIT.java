/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.connectors.starter.test.it;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.cloud.connectors.starter.model.IntegrationRequestEvent;
import org.activiti.cloud.connectors.starter.model.IntegrationResultEvent;
import org.activiti.cloud.services.api.commands.StartProcessInstanceCmd;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.test.junit.rabbit.RabbitTestSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableBinding({RuntimeMockStreams.class, MockProcessEngineChannels.class})
public class ActivitiCloudConnectorServiceIT {

    @Autowired
    private MessageChannel integrationEventsProducer;

    @ClassRule
    public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

    public static AtomicInteger integrationResultEventsCounter = new AtomicInteger();
    public static boolean startProcessInstanceCmdArrived = false;

    public final static String PROCESS_INSTANCE_ID = "processInstanceId-" + UUID.randomUUID().toString();
    public final static String PROCESS_DEFINITION_ID = "myProcessDefinitionId";
    public final static String EXECUTION_ID = "executionId-" + UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {

    }

    @EnableAutoConfiguration
    public static class StreamHandler {

        @StreamListener(value = RuntimeMockStreams.INTEGRATION_RESULT_CONSUMER)
        public void consumeIntegrationResultsMock(IntegrationResultEvent integrationResultEvent) throws InterruptedException {

            assertThat(integrationResultEvent.getVariables().get("var2")).isEqualTo(2);
            assertThat(integrationResultEvent.getExecutionId()).isEqualTo(EXECUTION_ID);
            integrationResultEventsCounter.incrementAndGet();
        }

        @StreamListener(value = MockProcessEngineChannels.COMMAND_CONSUMER)
        public void consumeProcessRuntimeCmd(StartProcessInstanceCmd startProcessInstanceCmd) throws InterruptedException {

            assertThat(startProcessInstanceCmd.getVariables().get("var2")).isEqualTo(2);
            assertThat(startProcessInstanceCmd.getProcessDefinitionId()).isEqualTo("MyOtherProcessDef");

            startProcessInstanceCmdArrived = true;
        }
    }

    @Test
    public void integrationEventShouldBePickedByConnectorMock() throws Exception {
        //given

        Map<String, Object> variables = new HashMap<>();
        variables.put("var1",
                      "value1");
        variables.put("var2",
                      new Long(1));

        IntegrationRequestEvent ire = new IntegrationRequestEvent(PROCESS_INSTANCE_ID,
                                                                  PROCESS_DEFINITION_ID,
                                                                  EXECUTION_ID,
                                                                  variables);

        Message<IntegrationRequestEvent> message = MessageBuilder.withPayload(ire)
                .setHeader("type",
                           "Mock")
                .build();
        integrationEventsProducer.send(message);

        message = MessageBuilder.withPayload(ire)
                .setHeader("type",
                           "MockProcessRuntime")
                .build();
        integrationEventsProducer.send(message);

        while (!startProcessInstanceCmdArrived) {
            System.out.println("Waiting for cmd to arrive ...");
            Thread.sleep(100);
        }

        assertThat(startProcessInstanceCmdArrived).isTrue();

        while (integrationResultEventsCounter.get() < 2) {
            System.out.println("Waiting for results to arrive ...");
            Thread.sleep(100);
        }
    }
}


