package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.entity.Status;
import by.abram.astore.exception.BusinessLogicException;
import by.abram.astore.exception.ResourceNotFoundException;
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
import org.springframework.data.jpa.domain.Specification;


import jakarta.persistence.criteria.JoinType;
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
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Status.NEW);

        if (dto.getItems() != null) {
            for (var itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemDto.getProductId()));
                if (product.getQuantity() < itemDto.getQuantity()) {
                    throw new BusinessLogicException(
                            String.format("Недостаточно товара '%s' на складе. В наличии: %d, запрошено: %d",
                                    product.getName(), product.getQuantity(), itemDto.getQuantity())
                    );
                }

                product.setQuantity(product.getQuantity() - itemDto.getQuantity());
                productRepository.save(product);

                Item item = new Item();
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity());
                item.setPrice(product.getPrice());
                item.setProductName(product.getName());
                order.addItem(item);
            }
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
    public Page<OrderDto> findAll(int page, int size, String query) {
        if (query == null || query.isBlank()) {
            return orderRepository.findAll(PageRequest.of(page, size))
                    .map(orderMapper::toDto);
        }

        return orderRepository.findAll(buildSearchSpecification(query.trim()), PageRequest.of(page, size))
                .map(orderMapper::toDto);
    }

    private Specification<Order> buildSearchSpecification(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            var userJoin = root.join("user", JoinType.LEFT);
            String likeQuery = "%" + query.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)), likeQuery),
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), likeQuery),
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("firstName")), likeQuery),
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("lastName")), likeQuery)
            );
        };
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
