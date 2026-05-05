package by.abram.astore;

import by.abram.astore.config.DemoDataSeeder;
import by.abram.astore.entity.Cart;
import by.abram.astore.entity.CartItem;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    void run_ShouldClearDatabaseAndSeedDemoCatalog() {
        Product staleProduct = new Product();
        staleProduct.getCategories().add(new Category());
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

        verify(cartItemRepository).deleteAllInBatch();
        verify(cartRepository).deleteAllInBatch();
        verify(itemRepository).deleteAllInBatch();
        verify(orderRepository).deleteAllInBatch();
        verify(categoryRepository).deleteAllInBatch();
        verify(userRepository).deleteAllInBatch();
        assertTrue(staleProduct.getCategories().isEmpty());

        ArgumentCaptor<Iterable<Category>> categoryCaptor = iterableCaptor();
        verify(categoryRepository).saveAll(categoryCaptor.capture());
        List<Category> categories = toList(categoryCaptor.getValue());

        assertEquals(6, categories.size());
        assertTrue(categories.stream().map(Category::getName).toList().containsAll(List.of(
                "Электроника",
                "Книги",
                "Товары для дома",
                "Товары для ванной и умывания",
                "Одежда",
                "Обувь"
        )));

        ArgumentCaptor<Iterable<Product>> productCaptor = iterableCaptor();
        verify(productRepository, times(2)).saveAll(productCaptor.capture());
        List<Product> products = toList(productCaptor.getAllValues().get(1));

        assertEquals(26, products.size());
        assertTrue(products.stream().allMatch(product -> product.getPrice().compareTo(BigDecimal.ZERO) > 0));
        assertTrue(products.stream().allMatch(product -> !product.getCategories().isEmpty()));
        assertTrue(products.stream().map(Product::getName).toList().containsAll(List.of(
                "Смартфон Aurora X",
                "Книга «Java без паники»",
                "Кроссовки Urban Run"
        )));

        ArgumentCaptor<Iterable<User>> userCaptor = iterableCaptor();
        verify(userRepository).saveAll(userCaptor.capture());
        List<User> users = toList(userCaptor.getValue());

        assertEquals(5, users.size());
        assertTrue(users.stream().allMatch(user -> "demo-password".equals(user.getPassword())));
        assertTrue(users.stream().map(User::getEmail).toList().containsAll(List.of(
                "marta.sokolova@astore.local",
                "sofia.romanova@astore.local"
        )));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(5)).save(orderCaptor.capture());

        for (Order order : orderCaptor.getAllValues()) {
            assertNotNull(order.getUser());
            assertNotNull(order.getOrderDate());
            assertNotNull(order.getStatus());
            assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
            assertEquals(3, order.getItems().size());
            order.getItems().forEach(item -> {
                assertSame(order, item.getOrder());
                assertNotNull(item.getProduct());
                assertNotNull(item.getProductName());
                assertNotNull(item.getPrice());
            });
        }

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(3)).save(cartCaptor.capture());

        for (Cart cart : cartCaptor.getAllValues()) {
            assertNotNull(cart.getUser());
            assertEquals(2, cart.getItems().size());
            for (CartItem item : cart.getItems()) {
                assertSame(cart, item.getCart());
                assertNotNull(item.getProduct());
                assertTrue(item.getQuantity() > 0);
            }
        }
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
}
