package by.abram.astore.mapper;

import by.abram.astore.dto.ItemDto;
import by.abram.astore.dto.OrderDto;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Item;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setUserId(order.getUser().getId());

        dto.setItems(order.getItems().stream()
                .map(this::itemToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    public ItemDto itemToDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        return dto;
    }
}