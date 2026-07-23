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

        // ADIM 1: Tum itemlari KONTROL et (henuz stok dusurme)
        List<Stock> stocksToUpdate = new ArrayList<>();

        for (Order.OrderItem item : order.getItems()) {
            Optional<Stock> stockOptional = stockRepository.findByProduct(item.getProduct());

            if (stockOptional.isEmpty()) {
                // Bu urun depoda hic yok -> tum siparis reddedilir
                sendResult(order, "REDDEDILDI", "Urun bulunamadi: " + item.getProduct());
                return;
            }

            Stock stock = stockOptional.get();

            if (stock.getQuantity() < item.getQuantity()) {
                // Yetersiz stok -> tum siparis reddedilir
                sendResult(order, "REDDEDILDI",
                        "Yetersiz stok: " + item.getProduct()
                                + " (istenen: " + item.getQuantity()
                                + ", mevcut: " + stock.getQuantity() + ")");
                return;
            }

            // Bu item uygun - dusurulecekler listesine ekle
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stocksToUpdate.add(stock);
        }

        // ADIM 2: Buraya geldiysek TUM itemlar uygun -> stoklari kaydet
        stockRepository.saveAll(stocksToUpdate);
        sendResult(order, "ONAYLANDI", "Tum urunler stokta mevcut");
    }

    // Sonucu olusturup Kafka'ya yollayan yardimci metot
    private void sendResult(Order order, String status, String reason) {
        OrderResult result = new OrderResult();
        result.setOrderId(order.getOrderId());
        result.setStatus(status);
        result.setReason(reason);
        resultProducer.sendResult(result);
        System.out.println("    SONUC: " + status + " - " + reason);
    }
}