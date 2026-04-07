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
    void update_ShouldRecalculateTotal() {
        Item item = new Item();
        Order order = mock(Order.class);
        item.setOrder(order);
        ItemDto dto = new ItemDto();
        dto.setQuantity(10);
        dto.setPrice(BigDecimal.TEN);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(order.calculateTotal()).thenReturn(BigDecimal.valueOf(100));

        itemService.update(1L, dto);

        verify(order).setTotalAmount(any());
        verify(orderRepository).save(order);
    }

    @Test
    void delete_ShouldUpdateOrder() {
        Item item = new Item();
        Order order = new Order();
        order.setItems(new ArrayList<>(List.of(item)));
        item.setOrder(order);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L);

        verify(itemRepository).delete(item);
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void create_Success() {
        Long orderId = 1L;
        ItemDto dto = new ItemDto();
        dto.setProductId(2L);
        dto.setQuantity(5);

        Order order = new Order();
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);
        Item item = new Item();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(new ItemDto());

        itemService.create(orderId, dto);

        verify(itemRepository).save(item);
        verify(orderRepository).save(order);
        assertEquals(product.getPrice(), item.getPrice());
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> itemService.findById(1L));
    }

    @Test
    void create_ShouldKeepPriceFromDto_WhenItIsSet() {
        ItemDto dto = new ItemDto();
        dto.setProductId(1L);
        dto.setQuantity(1);

        Order order = new Order();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(100));

        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(25));

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(new ItemDto());

        itemService.create(10L, dto);

        assertEquals(BigDecimal.valueOf(25), item.getPrice());
    }

    @Test
    void create_ShouldThrow_WhenOrderNotFound() {
        ItemDto dto = new ItemDto();
        dto.setProductId(1L);
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.create(99L, dto));
    }

    @Test
    void create_ShouldThrow_WhenProductNotFound() {
        ItemDto dto = new ItemDto();
        dto.setProductId(1L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(new Order()));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.create(5L, dto));
    }

    @Test
    void findById_ShouldReturnDto_WhenFound() {
        Item item = new Item();
        ItemDto dto = new ItemDto();
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(dto);

        ItemDto result = itemService.findById(2L);

        assertSame(dto, result);
    }

    @Test
    void findAll_ShouldReturnMappedPage() {
        Item item = new Item();
        ItemDto dto = new ItemDto();
        Page<Item> page = new PageImpl<>(List.of(item));

        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(itemMapper.toDto(item)).thenReturn(dto);

        Page<ItemDto> result = itemService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_ShouldKeepCurrentPrice_WhenDtoPriceIsNull() {
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(20));
        Order order = mock(Order.class);
        item.setOrder(order);

        ItemDto dto = new ItemDto();
        dto.setQuantity(3);
        dto.setPrice(null);

        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(order.calculateTotal()).thenReturn(BigDecimal.valueOf(60));
        when(itemMapper.toDto(item)).thenReturn(new ItemDto());

        itemService.update(3L, dto);

        assertEquals(BigDecimal.valueOf(20), item.getPrice());
    }

    @Test
    void update_ShouldThrow_WhenItemNotFound() {
        when(itemRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.update(7L, new ItemDto()));
    }

    @Test
    void delete_ShouldThrow_WhenItemNotFound() {
        when(itemRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.delete(8L));
    }
}
