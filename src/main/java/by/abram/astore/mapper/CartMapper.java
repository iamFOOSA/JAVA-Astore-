package by.abram.astore.mapper;

import by.abram.astore.dto.CartDto;
import by.abram.astore.dto.CartItemDto;
import by.abram.astore.entity.Cart;
import by.abram.astore.entity.CartItem;
import by.abram.astore.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setItems(cart.getItems().stream()
                .map(this::itemToDto)
                .collect(Collectors.toList()));
        dto.setTotalQuantity(dto.getItems().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum());
        dto.setTotalAmount(dto.getItems().stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return dto;
    }

    private CartItemDto itemToDto(CartItem item) {
        Product product = item.getProduct();
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductImageUrl(product.getImageUrl());
        dto.setProductPrice(product.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
