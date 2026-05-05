package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ItemDto;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.*;
import by.abram.astore.exception.BusinessLogicException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void create_Success() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        dto.setItems(List.of(itemDto));

        User user = new User();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(10));
        product.setQuantity(10);
        product.setName("Test Product");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderMapper.toDto(any())).thenReturn(dto);

        OrderDto result = orderService.create(dto);

        assertNotNull(result);
        assertEquals(8, product.getQuantity());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void create_UserNotFound() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.create(dto));
    }

    @Test
    void create_ProductNotFound() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(1L);
        dto.setItems(List.of(itemDto));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(dto));
    }

    @Test
    void create_InsufficientStock() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        ItemDto itemDto = new ItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(100);
        dto.setItems(List.of(itemDto));

        Product product = new Product();
        product.setQuantity(10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessLogicException.class, () -> orderService.create(dto));
    }


    @Test
    void findById_Success() {
        Order order = new Order();
        OrderDto dto = new OrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(dto);

        OrderDto result = orderService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void findById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.findById(1L));
    }

    @Test
    void create_WhenItemsIsNull_ShouldCreateOrderWithoutItems() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setItems(null);

        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(dto);

        OrderDto result = orderService.create(dto);

        assertNotNull(result);
        verify(productRepository, never()).findById(anyLong());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void findAll_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Order())));
        Page<OrderDto> result = orderService.findAll(0, 10, null);
        assertNotNull(result);
    }

    @Test
    void updateStatus_Success() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(new OrderDto());

        orderService.updateStatus(1L, Status.NEW);

        assertEquals(Status.NEW, order.getStatus());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void updateStatus_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.updateStatus(1L, Status.NEW));
    }

    @Test
    void delete_Success() {
        orderService.delete(1L);
        verify(orderRepository).deleteById(1L);
        verify(productCacheService).invalidateCache();
    }
}
