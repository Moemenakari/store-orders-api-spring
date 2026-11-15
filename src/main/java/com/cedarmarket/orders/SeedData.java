package com.cedarmarket.orders;

import com.cedarmarket.orders.product.Product;
import com.cedarmarket.orders.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() > 0) {
                return;
            }
            products.saveAll(List.of(
                    new Product("Olive Oil 750ml", "Pantry", new BigDecimal("12.50"), 40),
                    new Product("Zaatar Mix 250g", "Pantry", new BigDecimal("4.75"), 80),
                    new Product("Pomegranate Molasses", "Pantry", new BigDecimal("6.20"), 35),
                    new Product("Turkish Coffee 500g", "Drinks", new BigDecimal("9.90"), 50),
                    new Product("Rose Water 300ml", "Drinks", new BigDecimal("3.40"), 60),
                    new Product("Baklava Box 12pc", "Sweets", new BigDecimal("15.00"), 20)));
        };
    }
}
