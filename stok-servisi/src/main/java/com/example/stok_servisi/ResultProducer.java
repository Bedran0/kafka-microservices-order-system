package com.example.stok_servisi;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ResultProducer {

    private static final String TOPIC = "inventory-events";

    private final KafkaTemplate<String, OrderResult> kafkaTemplate;

    public ResultProducer(KafkaTemplate<String, OrderResult> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResult(OrderResult result) {
        kafkaTemplate.send(TOPIC, result.getOrderId(), result);
        System.out.println(">>> Sonuc gonderildi: " + result.getStatus() + " (" + result.getReason() + ")");
    }
}