package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.entity.Status;
import by.abram.astore.mapper.OrderMapper;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ProductCacheService productCacheService;

    @Transactional
    public OrderDto create(OrderDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.NEW);

        if (dto.getItems() != null) {
            dto.getItems().forEach(itemDto -> {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                Item item = new Item();
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(product.getPrice());
                item.setProductName(product.getName());
                order.addItem(item);
            });
        }

        order.setTotalAmount(order.calculateTotal());
        Order savedOrder = orderRepository.save(order);

        productCacheService.invalidateCache();

        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id, id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size))
                .map(orderMapper::toDto);
    }

    @Transactional
    public OrderDto updateStatus(Long id, Status newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        productCacheService.invalidateCache();

        return orderMapper.toDto(updatedOrder);
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
        productCacheService.invalidateCache();
    }
}