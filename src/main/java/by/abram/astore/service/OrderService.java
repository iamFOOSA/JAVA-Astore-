package by.abram.astore.service;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

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
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id, id: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto updateStatus(Long id, Status newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(newStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }
}