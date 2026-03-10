package by.abram.astore.service;

import by.abram.astore.dto.ItemDto;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ItemMapper;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ItemMapper itemMapper;
    private final String exceptionText = "Item not found, id: ";

    @Transactional
    public ItemDto create(Long orderId, ItemDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found, id: " + orderId));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found, id: " + dto.getProductId()));

        Item item = itemMapper.toEntity(dto);
        item.setProduct(product);
        item.setOrder(order);

        if (item.getPrice() == null) {
            item.setPrice(product.getPrice());
        }

        Item savedItem = itemRepository.save(item);

        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);

        return itemMapper.toDto(savedItem);
    }

    @Transactional(readOnly = true)
    public ItemDto findById(Long id) {
        return itemRepository.findById(id)
                .map(itemMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(exceptionText + id));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toDto)
                .toList();
    }

    @Transactional
    public ItemDto update(Long id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(exceptionText + id));

        item.setQuantity(dto.getQuantity());

        if (dto.getPrice() != null) {
            item.setPrice(dto.getPrice());
        }

        Item updatedItem = itemRepository.save(item);

        Order order = updatedItem.getOrder();
        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);

        return itemMapper.toDto(updatedItem);
    }

    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(exceptionText + id));

        Order order = item.getOrder();

        itemRepository.delete(item);

        order.getItems().remove(item);
        order.setTotalAmount(order.calculateTotal());
        orderRepository.save(order);
    }
}