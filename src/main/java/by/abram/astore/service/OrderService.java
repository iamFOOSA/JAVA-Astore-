package by.abram.astore.service;

import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Status;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.entity.Item;
import by.abram.astore.mapper.OrderMapper;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllWithDetails().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(Long userId, List<Long> productIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.NEW);
        order.setUser(user);

        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            Item item = new Item();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(1);
            item.setPrice(product.getPrice());

            order.addItem(item);
        }

        order.setTotalAmount(order.calculateTotal());
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }
}