package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "DTO корзины покупателя")
public class CartDto {

    @Schema(description = "Уникальный ID корзины", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID покупателя", example = "1")
    private Long userId;

    @Schema(description = "Общее количество товаров", example = "4")
    private Integer totalQuantity;

    @Schema(description = "Общая сумма корзины", example = "389.00")
    private BigDecimal totalAmount;

    @Schema(description = "Товары в корзине")
    private List<CartItemDto> items = new ArrayList<>();
}
