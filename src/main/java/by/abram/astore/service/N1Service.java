package by.abram.astore.service;

import by.abram.astore.entity.Product;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class N1Service {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public void showN1Problem() {
        List<Product> products = productRepository.findAll();

        System.out.println(".  ДЕМОНСТРАЦИЯ N+1 ПРОБЛЕМЫ ");
        System.out.println("Загружено " + products.size() + " продуктов");

        for (Product product : products) {
            System.out.println("Product: " + product.getName() +
                    ", Categories: " + product.getCategories().size());
        }
        System.out.println("   N+1 ПРОБЛЕМА ДЕМОНСТРИРОВАНА");
    }

    @Transactional(readOnly = true)
    public void solveN1Problem() {
        List<Product> products = productRepository.findAllWithDetails();

        System.out.println("    ДЕМОНСТРАЦИЯ РЕШЕНИЯ N+1    ");
        System.out.println("Загружено " + products.size() + " продуктов");

        for (Product product : products) {
            System.out.println("Product: " + product.getName() +
                    ", Categories: " + product.getCategories().size());
        }
        System.out.println("    N+1 ПРОБЛЕМА РЕШЕНА    ");
    }
}