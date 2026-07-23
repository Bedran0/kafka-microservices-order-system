package com.example.siparis_servisi;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResultConsumer {

    private final OrderRepository orderRepository;

    public ResultConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "inventory-events", groupId = "siparis-servisi-group")
    public void handleResult(OrderResult result) {
        System.out.println(">>> Sonuc geldi: " + result.getOrderId() + " -> " + result.getStatus());

        Optional<Order> orderOptional = orderRepository.findById(result.getOrderId());

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(result.getStatus());
            order.setReason(result.getReason());
            orderRepository.save(order);
            System.out.println("    Siparis guncellendi: " + order.getStatus());
        } else {
            System.out.println("    UYARI: Siparis bulunamadi: " + result.getOrderId());
        }
    }
}
