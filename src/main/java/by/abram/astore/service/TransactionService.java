package by.abram.astore.service;

import by.abram.astore.entity.Category;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.Status;
import by.abram.astore.entity.User;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public void saveWithoutTransaction() {
        System.out.println("=== НАЧАЛО ОПЕРАЦИИ БЕЗ ТРАНЗАКЦИИ ===");

        Category electronics = categoryRepository.findByName("transaction-test")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("transaction-test");
                    cat.setDescription("Категория для тестирования транзакций");
                    return categoryRepository.save(cat);
                });

        Product product1 = new Product();
        product1.setName("Transaction Test Product 1");
        product1.setDescription("Продукт для тестирования транзакций");
        product1.setPrice(new BigDecimal("100"));
        product1.setQuantity(10);
        product1.addCategory(electronics);
        productRepository.save(product1);
        System.out.println("Product 1 сохранен");

        User user = new User();
        user.setEmail("transaction-test@example.com");
        user.setPassword("password");
        user.setFirstName("Transaction");
        user.setLastName("Test");
        userRepository.save(user);
        System.out.println("User сохранен");

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.NEW);
        order.setUser(user);
        orderRepository.save(order);
        System.out.println("Order сохранен");

        Item item = new Item();
        item.setQuantity(2);
        item.setPrice(new BigDecimal("100"));
        item.setProduct(product1);
        item.setOrder(order);
        itemRepository.save(item);
        System.out.println("Item сохранен");

        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);
        System.out.println("Order обновлен с итоговой суммой");

        throw new RuntimeException("Искусственная ошибка для демонстрации транзакций!");
    }

    @Transactional
    public void saveWithTransaction() {
        System.out.println("=== НАЧАЛО ОПЕРАЦИИ С ТРАНЗАКЦИЕЙ ===");

        Category electronics = categoryRepository.findByName("transaction-test")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("transaction-test");
                    cat.setDescription("Категория для тестирования транзакций");
                    return categoryRepository.save(cat);
                });

        Product product1 = new Product();
        product1.setName("Transaction Test Product 1");
        product1.setDescription("Продукт для тестирования транзакций");
        product1.setPrice(new BigDecimal("100"));
        product1.setQuantity(10);
        product1.addCategory(electronics);
        productRepository.save(product1);
        System.out.println("Product 1 сохранен");

        User user = new User();
        user.setEmail("transaction-test@example.com");
        user.setPassword("password");
        user.setFirstName("Transaction");
        user.setLastName("Test");
        userRepository.save(user);
        System.out.println("User сохранен");

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.NEW);
        order.setUser(user);
        orderRepository.save(order);
        System.out.println("Order сохранен");

        Item item = new Item();
        item.setQuantity(2);
        item.setPrice(new BigDecimal("100"));
        item.setProduct(product1);
        item.setOrder(order);
        itemRepository.save(item);
        System.out.println("Item сохранен");

        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);
        System.out.println("Order обновлен с итоговой суммой");

    }
}