package by.abram.astore;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private static final String MACBOOK = "Apple MacBook Pro 16\"";

    @Override
    @Transactional
    public void run(String... args) {
        createCategories();
        createProducts();
        createUsers();
        createOrders();
    }

    private void createCategories() {
        createCategoryIfNotExists("Electronics", "electronic devices and gadgets");
        createCategoryIfNotExists("Smartphones", "Premium smartphones from leading brands");
        createCategoryIfNotExists("Laptops", "Professional laptops for work and gaming");
        createCategoryIfNotExists("Cameras", "Digital cameras and photography equipment");
    }

    private void createCategoryIfNotExists(String name, String description) {
        categoryRepository.findByName(name).ifPresentOrElse(
                category -> {
                },
                () -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    categoryRepository.save(category);
                }
        );
    }

    private void createProducts() {
        Category electronics = categoryRepository.findByName("Electronics").orElseThrow();
        Category smartphones = categoryRepository.findByName("Smartphones").orElseThrow();
        Category laptops = categoryRepository.findByName("Laptops").orElseThrow();
        Category cameras = categoryRepository.findByName("Cameras").orElseThrow();

        createProductIfNotExists("Apple iPhone 15 Pro",
                "Apple iPhone 15 Pro, 256GB, Titanium Black - 6.1\" OLED display, Apple A17 Pro chip",
                new BigDecimal("119990"), 45, Arrays.asList(electronics, smartphones));

        createProductIfNotExists("Samsung Galaxy S24 Ultra",
                "Samsung Galaxy S24 Ultra, 512GB, Black - 6.8\" Dynamic AMOLED, Snapdragon 8 Gen 3",
                new BigDecimal("129990"), 30, Arrays.asList(electronics, smartphones));

        createProductIfNotExists("MACBOOK\"",
                "MACBOOK\" M3 Max, 32GB, 1TB - Liquid Retina XDR display, 12-core M3 Max processor",
                new BigDecimal("349990"), 15, Arrays.asList(electronics, laptops));

        createProductIfNotExists("Sony A7IV",
                "Sony A7IV Digital Camera, Black - 33MP full-frame sensor, 4K 60p video",
                new BigDecimal("249990"), 10, Arrays.asList(electronics, cameras));
    }

    private void createProductIfNotExists(String name, String description,
                                          BigDecimal price, Integer quantity,
                                          List<Category> categories) {
        productRepository.findByName(name).ifPresentOrElse(
                product -> {
                },
                () -> {
                    Product product = new Product();
                    product.setName(name);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setQuantity(quantity);

                    for (Category category : categories) {
                        product.addCategory(category);
                    }

                    productRepository.save(product);
                }
        );
    }

    private void createUsers() {
        createUserIfNotExists("john.doe@example.com", "password123", "John", "Doe");
        createUserIfNotExists("jane.smith@example.com", "password123", "Jane", "Smith");
    }

    private void createUserIfNotExists(String email, String password,
                                       String firstName, String lastName) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                },
                () -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    userRepository.save(user);
                }
        );
    }

    private void createOrders() {
        User john = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        User jane = userRepository.findByEmail("jane.smith@example.com").orElseThrow();

        createOrder(john, LocalDateTime.of(2026, 2, 15, 10, 30), Status.NEW,
                new ProductOrder("Apple iPhone 15 Pro", 1),
                new ProductOrder("Sony A7IV", 1));

        createOrder(john, LocalDateTime.of(2026, 2, 20, 14, 45), Status.PROCESSING,
                new ProductOrder("MACBOOK\"", 1));

        createOrder(jane, LocalDateTime.of(2026, 2, 18, 11, 20), Status.DELIVERED,
                new ProductOrder("Samsung Galaxy S24 Ultra", 1),
                new ProductOrder("MACBOOK\"", 1));
    }

    private void createOrder(User user, LocalDateTime orderDate, Status status,
                             ProductOrder... productOrders) {
        Order order = new Order();
        order.setOrderDate(orderDate);
        order.setStatus(status);
        order.setUser(user);
        user.addOrder(order);

        for (ProductOrder po : productOrders) {
            Product product = productRepository.findByName(po.productName)
                    .orElseThrow();

            Item item = new Item();
            item.setQuantity(po.quantity);
            item.setPrice(product.getPrice());
            item.setProduct(product);
            item.setOrder(order);

            order.addItem(item);
        }

        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);
    }

    private static class ProductOrder {
        String productName;
        int quantity;

        public ProductOrder(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }
    }
}