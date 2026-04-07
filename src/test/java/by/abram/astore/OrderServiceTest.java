package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ItemDto;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.mapper.OrderMapper;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import by.abram.astore.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private ProductCacheService productCacheService;

    @InjectMocks private OrderService orderService;

    @Test
    void create_WithItems_ShouldCalculateTotal() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        dto.setItems(List.of(itemDto));

        User user = new User();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(50));
        product.setQuantity(100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.create(dto);

        verify(orderRepository).save(argThat(order ->
                order.getTotalAmount().compareTo(BigDecimal.valueOf(100)) == 0));
    }
}