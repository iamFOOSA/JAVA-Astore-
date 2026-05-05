package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ItemDto;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ItemMapper;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ItemService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ItemMapper itemMapper;
    @Mock private ProductCacheService productCacheService;

    @InjectMocks private ItemService itemService;

    @Test
    void create_Success_WithNullItemPrice() {
        Long orderId = 1L;
        ItemDto dto = new ItemDto();
        dto.setProductId(2L);

        Order order = mock(Order.class);
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(150));

        Item item = new Item();
        item.setPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(dto);

        itemService.create(orderId, dto);

        assertEquals(BigDecimal.valueOf(150), item.getPrice());
        verify(order).calculateTotal();
        verify(productCacheService).invalidateCache();
    }

    @Test
    void create_WhenItemPriceIsNull_ShouldUseProductPrice() {
        Long orderId = 1L;
        ItemDto dto = new ItemDto();
        dto.setProductId(10L);
        dto.setPrice(null);

        Order order = mock(Order.class);
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(500));
        product.setName("Test Product");

        Item item = new Item();
        item.setPrice(null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(dto);

        itemService.create(orderId, dto);

        assertEquals(BigDecimal.valueOf(500), item.getPrice());
        verify(itemRepository).save(item);
    }

    @Test
    void create_OrderNotFound() {
        ItemDto dto = new ItemDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.create(1L, dto));
    }

    @Test
    void create_ProductNotFound() {
        ItemDto dto = new ItemDto();
        dto.setProductId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.create(1L, dto));
    }

    @Test
    void create_WhenItemPriceIsProvided_ShouldKeepDtoPrice() {
        Long orderId = 1L;
        ItemDto dto = new ItemDto();
        dto.setProductId(2L);
        BigDecimal customPrice = BigDecimal.valueOf(200);

        Order order = mock(Order.class);
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(150));

        Item item = new Item();
        item.setPrice(customPrice);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(dto);

        itemService.create(orderId, dto);

        assertEquals(customPrice, item.getPrice());
    }

    @Test
    void update_WhenPriceIsNull_ShouldNotUpdateItemPrice() {
        Long itemId = 1L;
        BigDecimal originalPrice = BigDecimal.valueOf(100);

        Item item = new Item();
        item.setPrice(originalPrice);
        item.setOrder(mock(Order.class));

        ItemDto updateDto = new ItemDto();
        updateDto.setQuantity(10);
        updateDto.setPrice(null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(updateDto);

        itemService.update(itemId, updateDto);

        assertEquals(10, item.getQuantity());
        assertEquals(originalPrice, item.getPrice());
    }

    @Test
    void findById_Success() {
        Item item = new Item();
        ItemDto dto = new ItemDto();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(dto);

        ItemDto result = itemService.findById(1L);

        assertNotNull(result);
        assertEquals(dto, result);
    }

    @Test
    void findById_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.findById(1L));
    }

    @Test
    void findAll_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Item())));
        Page<ItemDto> result = itemService.findAll(0, 10, null);
        assertNotNull(result);
        verify(itemMapper).toDto(any());
    }

    @Test
    void update_Success_WithNewPrice() {
        Item item = new Item();
        Order order = mock(Order.class);
        item.setOrder(order);
        ItemDto dto = new ItemDto();
        dto.setQuantity(5);
        dto.setPrice(BigDecimal.valueOf(100));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(dto);

        itemService.update(1L, dto);

        assertEquals(5, item.getQuantity());
        assertEquals(BigDecimal.valueOf(100), item.getPrice());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_NotFound() {
        ItemDto dto = new ItemDto();
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.update(1L, dto));
    }

    @Test
    void delete_Success() {
        Item item = new Item();
        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(item)));
        item.setOrder(order);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L);

        verify(itemRepository).delete(item);
        assertTrue(order.getItems().isEmpty());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void delete_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.delete(1L));
    }
}
