package by.abram.astore;

import by.abram.astore.entity.Product;
import by.abram.astore.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        Product product1 = new Product();
        product1.setName("Ноутбук");
        product1.setDescription("mac M3 Pro");
        product1.setPrice(new BigDecimal("7800"));
        product1.setQuantity(100);
        product1.setCategory("электрика");
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Смартфон");
        product2.setDescription("iPhone 14 Pro ");
        product2.setPrice(new BigDecimal("3700"));
        product2.setQuantity(250);
        product2.setCategory("электрика");
        productRepository.save(product2);

        Product product3 = new Product();
        product3.setName("Наушники");
        product3.setDescription("airPods 4 active noise");
        product3.setPrice(new BigDecimal("700"));
        product3.setQuantity(500);
        product3.setCategory("электрика");
        productRepository.save(product3);
    }
}