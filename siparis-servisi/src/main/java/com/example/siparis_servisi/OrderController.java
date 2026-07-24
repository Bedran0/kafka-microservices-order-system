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
        Customer customer = customerRepository.findByName(request.getCustomerName())
                .orElseGet(() -> new Customer(request.getCustomerName()));

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setStatus("BEKLEMEDE");
        order.setReason("Siparis alindi, stok kontrol ediliyor");
        order.setCustomer(customer);

        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem(itemReq.getProduct(), itemReq.getQuantity());
            item.setOrder(order);
            order.getItems().add(item);
        }

        customer.getOrders().add(order);
        customerRepository.save(customer);

        OrderMessage message = new OrderMessage();
        message.setOrderId(order.getOrderId());
        message.setCustomerName(customer.getName());
        for (OrderItem item : order.getItems()) {
            message.getItems().add(new OrderMessage.ItemMessage(item.getProduct(), item.getQuantity()));
        }

        orderProducer.sendOrder(message);

        return order;
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderRepository.findById(id).orElse(null);
    }
}
