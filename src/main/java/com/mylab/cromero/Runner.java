package com.mylab.cromero;

import com.mylab.cromero.receiver.FirstReceiver;
import com.mylab.cromero.receiver.SecondReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static com.mylab.cromero.Application.EXCHANGE_NAME;

@Component
public class Runner implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RabbitTemplate rabbitTemplate;

    private final FirstReceiver firstReceiver;

    private final SecondReceiver secondReceiver;

    private final ConfigurableApplicationContext context;


    public Runner(FirstReceiver firstReceiver,SecondReceiver secondReceiver,
                  RabbitTemplate rabbitTemplate,
                  ConfigurableApplicationContext context) {
        this.firstReceiver = firstReceiver;
        this.secondReceiver=secondReceiver;
        this.rabbitTemplate = rabbitTemplate;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Sending message...");

        rabbitTemplate.convertAndSend(EXCHANGE_NAME,null, "Hello from RabbitMQ Sent 1!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,null, "Hello from RabbitMQ Sent 2!");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,null, "Hello from RabbitMQ Sent 3!");

        context.close();
    }

}
