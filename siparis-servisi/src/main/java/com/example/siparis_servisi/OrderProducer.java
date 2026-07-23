package com.example.siparis_servisi;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(OrderMessage message) {
        kafkaTemplate.send(TOPIC, message.getOrderId(), message);
        System.out.println(">>> Order sent to Kafka: " + message.getOrderId() + " (" + message.getItems().size() + " items)");
    }
}