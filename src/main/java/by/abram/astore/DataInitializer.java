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
        product1.setName("laptop");
        product1.setDescription("mac M3 Pro");
        product1.setPrice(new BigDecimal("7800"));
        product1.setQuantity(100);
        product1.setCategory("electrics");
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("car");
        product2.setDescription("mini car");
        product2.setPrice(new BigDecimal("50"));
        product2.setQuantity(2500);
        product2.setCategory("toys");
        productRepository.save(product2);

        Product product3 = new Product();
        product3.setName("bike");
        product3.setDescription("mount-monster");
        product3.setPrice(new BigDecimal("700"));
        product3.setQuantity(500);
        product3.setCategory("sport");
        productRepository.save(product3);
    }
}