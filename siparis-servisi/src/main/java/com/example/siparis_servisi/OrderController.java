package com.example.siparis_servisi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderProducer orderProducer;

    public OrderController(OrderRepository orderRepository,
                           CustomerRepository customerRepository,
                           OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        // 1. Musteriyi bul, yoksa olustur
        Customer customer = customerRepository.findByName(request.getCustomerName())
                .orElseGet(() -> new Customer(request.getCustomerName()));

        // 2. Siparisi olustur ve musteriye bagla
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus("BEKLEMEDE");
        order.setReason("Siparis alindi, stok kontrol ediliyor");
        order.setCustomer(customer);

        // 3. Her item icin bir OrderItem olustur ve siparise bagla
        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem(itemReq.getProduct(), itemReq.getQuantity());
            item.setOrder(order);
            order.getItems().add(item);
        }

        // 4. Kaydet (cascade ile musteri + siparis + itemlar birlikte)
        customer.getOrders().add(order);
        customerRepository.save(customer);

        // 5. Kafka'ya gonderilecek mesaji hazirla
        OrderMessage message = new OrderMessage();
        message.setOrderId(order.getOrderId());
        message.setCustomerName(customer.getName());
        for (OrderItem item : order.getItems()) {
            message.getItems().add(new OrderMessage.ItemMessage(item.getProduct(), item.getQuantity()));
        }

        // 6. Kafka'ya yolla
        orderProducer.sendOrder(message);

        return order;
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderRepository.findById(id).orElse(null);
    }
}