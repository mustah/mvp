package com.elvaco.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Recv {
    private static final String QUEUE_NAME = "meter-messages";

    private static final int PREFETCH_COUNT = 1000;
    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Map<String, Object> args = new HashMap<>();
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        System.out.println(" [*] Waiting for messages. (^C exits)");
        channel.basicQos(PREFETCH_COUNT);
        Consumer consumer = new DbPublishingConsumer(channel, Math.min(100, PREFETCH_COUNT));
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }
}
