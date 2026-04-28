package by.abram.astore.controller;

import by.abram.astore.dto.CartDto;
import by.abram.astore.dto.CartItemRequest;
import by.abram.astore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Корзины", description = "Сохранённые корзины покупателей")
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить корзину покупателя")
    public ResponseEntity<CartDto> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.findByUser(userId));
    }

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Добавить товар в корзину")
    public ResponseEntity<CartDto> addItem(@PathVariable Long userId,
                                           @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/user/{userId}/items/{productId}")
    @Operation(summary = "Изменить количество товара в корзине")
    public ResponseEntity<CartDto> updateItem(@PathVariable Long userId,
                                              @PathVariable Long productId,
                                              @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userId, productId, request));
    }

    @DeleteMapping("/user/{userId}/items/{productId}")
    @Operation(summary = "Удалить товар из корзины")
    public ResponseEntity<CartDto> removeItem(@PathVariable Long userId,
                                              @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Очистить корзину покупателя")
    public ResponseEntity<CartDto> clear(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.clear(userId));
    }
}
