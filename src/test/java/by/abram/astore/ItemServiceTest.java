package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ItemDto;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.mapper.ItemMapper;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
}