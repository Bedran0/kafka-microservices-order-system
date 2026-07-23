package com.example.stok_servisi;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initStock(StockRepository stockRepository) {
        return args -> {
            // Eger veritabani bossa baslangic verilerini ekle
            if (stockRepository.count() == 0) {
                stockRepository.save(new Stock("keyboard", 10));
                stockRepository.save(new Stock("mouse", 3));
                stockRepository.save(new Stock("monitor", 5));
                System.out.println(">>> Baslangic stok verileri eklendi.");
            } else {
                System.out.println(">>> Stok verileri zaten mevcut, ekleme yapilmadi.");
            }
        };
    }
}