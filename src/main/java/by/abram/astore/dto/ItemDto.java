package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "DTO позиции в заказе")
public class ItemDto {

    @Schema(description = "Уникальный ID позиции", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    @Schema(description = "Количество товара", example = "2")
    private Integer quantity;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @Schema(description = "Цена товара на момент покупки", example = "999.99")
    private BigDecimal price;

    @NotNull(message = "ID продукта обязателен")
    @Schema(description = "ID продукта", example = "1")
    private Long productId;

    @Schema(description = "Название продукта", example = "iPhone 15")
    private String productName;
}