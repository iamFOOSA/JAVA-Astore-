package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на изменение товара в корзине")
public class CartItemRequest {

    @NotNull(message = "ID товара обязателен")
    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    @Schema(description = "Количество товара", example = "2")
    private Integer quantity;
}
