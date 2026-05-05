package by.abram.astore;

import by.abram.astore.config.DemoDataSeeder;
import by.abram.astore.entity.Cart;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.repository.CartItemRepository;
import by.abram.astore.repository.CartRepository;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    void run_ShouldClearDatabaseBeforeSeeding() {
        Product staleProduct = staleProduct();

        seedDemoCatalog(staleProduct);

        verify(cartItemRepository).deleteAllInBatch();
        verify(cartRepository).deleteAllInBatch();
        verify(itemRepository).deleteAllInBatch();
        verify(orderRepository).deleteAllInBatch();
        verify(categoryRepository).deleteAllInBatch();
        verify(userRepository).deleteAllInBatch();
        assertTrue(staleProduct.getCategories().isEmpty());
    }

    @Test
    void run_ShouldSeedCategoriesProductsAndUsers() {
        SeededDemoCatalog catalog = seedDemoCatalog(staleProduct());

        assertEquals(6, catalog.categories().size());
        assertTrue(catalog.categories().stream().map(Category::getName).toList().containsAll(List.of(
                "Электроника",
                "Книги",
                "Товары для дома",
                "Товары для ванной и умывания",
                "Одежда",
                "Обувь"
        )));

        assertEquals(26, catalog.products().size());
        assertTrue(catalog.products().stream().allMatch(
                product -> product.getPrice().compareTo(BigDecimal.ZERO) > 0));
        assertTrue(catalog.products().stream().allMatch(product -> !product.getCategories().isEmpty()));
        assertTrue(catalog.products().stream().map(Product::getName).toList().containsAll(List.of(
                "Смартфон Aurora X",
                "Книга «Java без паники»",
                "Кроссовки Urban Run"
        )));

        assertEquals(5, catalog.users().size());
        assertTrue(catalog.users().stream().allMatch(user -> "demo-password".equals(user.getPassword())));
        assertTrue(catalog.users().stream().map(User::getEmail).toList().containsAll(List.of(
                "marta.sokolova@astore.local",
                "sofia.romanova@astore.local"
        )));
    }

    @Test
    void run_ShouldCreateOrdersWithItems() {
        SeededDemoCatalog catalog = seedDemoCatalog(staleProduct());

        assertEquals(5, catalog.orders().size());
        assertTrue(catalog.orders().stream().allMatch(this::isValidSeededOrder));
    }

    @Test
    void run_ShouldCreateCartsWithItems() {
        SeededDemoCatalog catalog = seedDemoCatalog(staleProduct());

        assertEquals(3, catalog.carts().size());
        assertTrue(catalog.carts().stream().allMatch(this::isValidSeededCart));
    }

    private SeededDemoCatalog seedDemoCatalog(Product staleProduct) {
        when(productRepository.findAll()).thenReturn(List.of(staleProduct));

        new DemoDataSeeder(
                userRepository,
                categoryRepository,
                productRepository,
                orderRepository,
                itemRepository,
                cartRepository,
                cartItemRepository
        ).run();

        return new SeededDemoCatalog(
                capturedCategories(),
                capturedProducts(),
                capturedUsers(),
                capturedOrders(),
                capturedCarts()
        );
    }

    private Product staleProduct() {
        Product product = new Product();
        product.getCategories().add(new Category());
        return product;
    }

    private List<Category> capturedCategories() {
        ArgumentCaptor<Iterable<Category>> categoryCaptor = iterableCaptor();
        verify(categoryRepository).saveAll(categoryCaptor.capture());
        return toList(categoryCaptor.getValue());
    }

    private List<Product> capturedProducts() {
        ArgumentCaptor<Iterable<Product>> productCaptor = iterableCaptor();
        verify(productRepository, times(2)).saveAll(productCaptor.capture());
        return toList(productCaptor.getAllValues().get(1));
    }

    private List<User> capturedUsers() {
        ArgumentCaptor<Iterable<User>> userCaptor = iterableCaptor();
        verify(userRepository).saveAll(userCaptor.capture());
        return toList(userCaptor.getValue());
    }

    private List<Order> capturedOrders() {
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(5)).save(orderCaptor.capture());
        return orderCaptor.getAllValues();
    }

    private List<Cart> capturedCarts() {
        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(3)).save(cartCaptor.capture());
        return cartCaptor.getAllValues();
    }

    private boolean isValidSeededOrder(Order order) {
        return order.getUser() != null
                && order.getOrderDate() != null
                && order.getStatus() != null
                && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                && order.getItems().size() == 3
                && order.getItems().stream().allMatch(item -> item.getOrder() == order
                && item.getProduct() != null
                && item.getProductName() != null
                && item.getPrice() != null);
    }

    private boolean isValidSeededCart(Cart cart) {
        return cart.getUser() != null
                && cart.getItems().size() == 2
                && cart.getItems().stream().allMatch(item -> item.getCart() == cart
                && item.getProduct() != null
                && item.getQuantity() > 0);
    }

    private static <T> List<T> toList(Iterable<T> values) {
        List<T> list = new ArrayList<>();
        values.forEach(list::add);
        return list;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<Iterable<T>> iterableCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Iterable.class);
    }

    private record SeededDemoCatalog(
            List<Category> categories,
            List<Product> products,
            List<User> users,
            List<Order> orders,
            List<Cart> carts
    ) {
    }
}
