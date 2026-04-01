package by.abram.astore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO пользователя")
public class UserDto {

    @Schema(description = "Уникальный ID пользователя", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат электронной почты")
    @Schema(description = "Электронная почта", example = "user@example.com")
    private String email;

    @NotBlank(message = "Имя обязательно")
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;
}