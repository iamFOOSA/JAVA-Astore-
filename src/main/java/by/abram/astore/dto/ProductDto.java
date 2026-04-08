package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Объект передачи данных для продукта")
public class ProductDto {

    @Schema(description = "Уникальный идентификатор продукта", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название продукта не может быть пустым")
    @Schema(description = "Название продукта", example = "Смартфон")
    private String name;

    @Schema(description = "Описание продукта", example = "Последняя модель с отличной камерой")
    private String description;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @Schema(description = "Цена продукта", example = "999.99")
    private BigDecimal price;

    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Schema(description = "Количество на складе", example = "50")
    private Integer quantity;

    @Schema(description = "Список категорий, к которым относится продукт")
    private List<String> categories;
}