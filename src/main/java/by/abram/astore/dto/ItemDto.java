package by.abram.astore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemDto {

    private Long id;

    private Integer quantity;

    private BigDecimal price;

    private Long productId;

    private String productName;
}