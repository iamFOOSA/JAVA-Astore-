package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO категории продуктов")
public class CategoryDto {

    @Schema(description = "Уникальный ID категории", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название категории не может быть пустым")
    @Schema(description = "Название", example = "Смартфоны")
    private String name;

    @Schema(description = "Описание категории", example = "Мобильные телефоны и аксессуары")
    private String description;
}