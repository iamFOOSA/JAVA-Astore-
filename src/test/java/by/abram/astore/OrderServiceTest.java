package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ItemDto;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.Status;
import by.abram.astore.entity.User;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.OrderMapper;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import by.abram.astore.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(orderMapper.toDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.create(dto);

        verify(orderRepository).save(argThat(order ->
                order.getTotalAmount().compareTo(BigDecimal.valueOf(100)) == 0));
        verify(productCacheService).invalidateCache();
    }

    @Test
    void create_ShouldThrow_WhenNotEnoughStock() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(1000);
        dto.setItems(List.of(itemDto));

        User user = new User();
        Product product = new Product();
        product.setQuantity(10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(by.abram.astore.exception.BusinessLogicException.class,
                () -> orderService.create(dto));
    }

    @Test
    void delete_Success() {
        orderService.delete(1L);
        verify(orderRepository).deleteById(1L);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void create_ShouldThrow_WhenUserNotFound() {
        OrderDto dto = new OrderDto();
        dto.setUserId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(dto));
    }

    @Test
    void create_ShouldThrow_WhenProductNotFound() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(99L);
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(dto));
    }

    @Test
    void create_WithoutItems_ShouldSaveOrderWithZeroTotal() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setItems(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderMapper.toDto(any(Order.class))).thenReturn(new OrderDto());

        orderService.create(dto);

        verify(orderRepository).save(argThat(order ->
                order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void findById_ShouldReturnDto_WhenFound() {
        Order order = new Order();
        OrderDto dto = new OrderDto();

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.findById(2L);

        assertSame(dto, result);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(orderRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.findById(3L));
    }

    @Test
    void findAll_ShouldReturnMappedPage() {
        Order order = new Order();
        OrderDto dto = new OrderDto();
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(dto);

        Page<OrderDto> result = orderService.findAll(0, 20);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateStatus_ShouldUpdateAndReturnDto() {
        Order order = new Order();
        order.setStatus(Status.NEW);
        OrderDto dto = new OrderDto();

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.updateStatus(4L, Status.SHIPPED);

        assertSame(dto, result);
        assertEquals(Status.SHIPPED, order.getStatus());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void updateStatus_ShouldThrow_WhenOrderMissing() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.updateStatus(5L, Status.CANCELLED));
    }
}
