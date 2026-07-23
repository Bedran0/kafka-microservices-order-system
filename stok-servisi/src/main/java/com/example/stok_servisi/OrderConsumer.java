package com.example.stok_servisi;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderConsumer {

    private final StockRepository stockRepository;
    private final ResultProducer resultProducer;

    public OrderConsumer(StockRepository stockRepository, ResultProducer resultProducer) {
        this.stockRepository = stockRepository;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "order-events", groupId = "stok-servisi-group-v2")
    public void handleOrder(Order order) {
        System.out.println(">>> Yeni siparis: " + order.getOrderId()
                + " | Musteri: " + order.getCustomerName()
                + " | " + order.getItems().size() + " item");

        // check all items
        List<Stock> stocksToUpdate = new ArrayList<>();

        for (Order.OrderItem item : order.getItems()) {
            Optional<Stock> stockOptional = stockRepository.findByProduct(item.getProduct());

            if (stockOptional.isEmpty()) {
                // if product doesn't exist in the db, reject entire order
                sendResult(order, "REDDEDILDI", "Urun bulunamadi: " + item.getProduct());
                return;
            }

            Stock stock = stockOptional.get();

            if (stock.getQuantity() < item.getQuantity()) {
                // if stock is insufficient, reject entire order
                sendResult(order, "REDDEDILDI",
                        "Yetersiz stok: " + item.getProduct()
                                + " (istenen: " + item.getQuantity()
                                + ", mevcut: " + stock.getQuantity() + ")");
                return;
            }

            // item is valid, so add to the list to be updated
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stocksToUpdate.add(stock);
        }

        // if we reached here, all items are valid. So, save stocks
        stockRepository.saveAll(stocksToUpdate);
        sendResult(order, "ONAYLANDI", "Tum urunler stokta mevcut");
    }

    // generates the result and sends it to Kafka
    private void sendResult(Order order, String status, String reason) {
        OrderResult result = new OrderResult();
        result.setOrderId(order.getOrderId());
        result.setStatus(status);
        result.setReason(reason);
        resultProducer.sendResult(result);
        System.out.println("    SONUC: " + status + " - " + reason);
    }
}
