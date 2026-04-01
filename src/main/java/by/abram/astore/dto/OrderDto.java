package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "DTO заказа")
public class OrderDto {

    @Schema(description = "Уникальный ID заказа", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Дата и время создания заказа")
    private LocalDateTime orderDate;

    @Schema(description = "Статус заказа", example = "CREATED")
    private String status;

    @Schema(description = "Общая сумма заказа", example = "1999.98")
    private BigDecimal totalAmount;

    @NotNull(message = "ID пользователя обязателен")
    @Schema(description = "ID пользователя, совершившего заказ", example = "1")
    private Long userId;

    @NotEmpty(message = "Заказ должен содержать хотя бы одну позицию")
    @Valid
    @Schema(description = "Список товаров в заказе")
    private List<ItemDto> items;
}