package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO товара в корзине")
public class CartItemDto {

    @Schema(description = "Уникальный ID позиции корзины", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @Schema(description = "Название товара", example = "Кроссовки Urban Run")
    private String productName;

    @Schema(description = "Картинка товара", example = "/product-images/sneakers.svg")
    private String productImageUrl;

    @Schema(description = "Текущая цена товара", example = "179.00")
    private BigDecimal productPrice;

    @Schema(description = "Количество товара в корзине", example = "2")
    private Integer quantity;
}
