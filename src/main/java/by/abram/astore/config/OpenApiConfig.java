package by.abram.astore.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Astore API",
                description = "REST API системы управления интернет-магазином",
                version = "1.0.0",
                contact = @Contact(
                        name = "Danila Abramchuk"
                )
        )
)
public class OpenApiConfig {
}