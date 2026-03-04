package by.abram.astore.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private List<String> categories;
}