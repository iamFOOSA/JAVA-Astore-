package by.abram.astore.service;

import by.abram.astore.entity.Product;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class N1Service {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public void showN1Problem() {
        List<Product> products = productRepository.findAll();

        log.info(".  ДЕМОНСТРАЦИЯ N+1 ПРОБЛЕМЫ ");
        log.info("Загружено {} продуктов", products.size());

        for (Product product : products) {
            log.info("Product: {}, Categories: {}",
                    product.getName(), product.getCategories().size());
        }
        log.info("   N+1 ПРОБЛЕМА ДЕМОНСТРИРОВАНА");
    }

    @Transactional(readOnly = true)
    public void solveN1Problem() {
        List<Product> products = productRepository.findAllWithDetails();

        log.info("    ДЕМОНСТРАЦИЯ РЕШЕНИЯ N+1    ");
        log.info("Загружено {} продуктов", products.size());

        for (Product product : products) {
            log.info("Product: {}, Categories: {}",
                    product.getName(), product.getCategories().size());
        }
        log.info("    N+1 ПРОБЛЕМА РЕШЕНА    ");
    }
}