package com.example.stok_servisi;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // Urun adina gore stok bulmak icin (Spring bu metodu otomatik yazar)
    Optional<Stock> findByProduct(String product);
}