/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mylab.cromero;

import com.google.common.io.Files;
import com.mylab.cromero.receiver.FirstReceiver;
import com.mylab.cromero.receiver.SecondReceiver;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.mylab.cromero.Application.EXCHANGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationIntegrationTest {

    @Value("${spring.rabbitmq.port}")
    private  String rabbitmqPort;


    public static final String QPID_CONFIG_LOCATION = "src/test/resources/qpid-config.json";
    public static final String APPLICATION_CONFIG_LOCATION = "src/test/resources/application.properties";

    @MockBean
    private Runner runner;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FirstReceiver firstReceiver;

    @Autowired
    private SecondReceiver secondReceiver;

    //TODO Extract external util class (Start(Stop Rabbitmq )
    @ClassRule public static final ExternalResource resource = new  ExternalResource() {
        private Broker broker = new Broker();

        @Override
        protected void before() throws Throwable {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(APPLICATION_CONFIG_LOCATION)));
            String amqpPort = properties.getProperty("spring.rabbitmq.port");
            File tmpFolder = Files.createTempDir();
            String userDir = System.getProperty("user.dir").toString();
            File file = new File(userDir);
            String homePath = file.getAbsolutePath();
            BrokerOptions brokerOptions = new BrokerOptions();
            brokerOptions.setConfigProperty("qpid.work_dir", tmpFolder.getAbsolutePath());
            brokerOptions.setConfigProperty("qpid.amqp_port", amqpPort);
            brokerOptions.setConfigProperty("qpid.home_dir", homePath);
            brokerOptions.setInitialConfigurationLocation(homePath + "/" + QPID_CONFIG_LOCATION);
            broker.startup(brokerOptions);
        }


        @Override
        protected void after() {
            broker.shutdown();
        }

    };



    @Test
    public void testWithFirstReceiverRoutingKey() throws Exception {
        firstReceiver.initCounter();
        secondReceiver.initCounter();
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,FirstReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 1!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,FirstReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 2!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,FirstReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 3!");
        Thread.sleep(5000);
        assertThat(firstReceiver.getCounter()).isEqualTo(3);
        assertThat(secondReceiver.getCounter()).isEqualTo(0);
    }

    @Test
    public void testMixReceiverRoutingKey() throws Exception {
        //TODO CREATE @ RULE TO BE ABLE TO INIT COUNTERS
        firstReceiver.initCounter();
        secondReceiver.initCounter();
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,FirstReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 1!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,SecondReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 2!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,FirstReceiver.QUEUE_ROUTINGKEY, "Hello from RabbitMQ Sent 3!");
        Thread.sleep(5000);
        assertThat(firstReceiver.getCounter()).isEqualTo(2);
        assertThat(secondReceiver.getCounter()).isEqualTo(1);
    }

    @Test
    public void testNoRoutingkey() throws Exception {
        firstReceiver.initCounter();
        secondReceiver.initCounter();
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,"routing_not_found", "Hello from RabbitMQ Sent 1!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,"routing_not_found", "Hello from RabbitMQ Sent 2!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,"routing_not_found", "Hello from RabbitMQ Sent 3!");
        Thread.sleep(5000);
        assertThat(firstReceiver.getCounter()).isEqualTo(0);
        assertThat(secondReceiver.getCounter()).isEqualTo(0);
    }

}
