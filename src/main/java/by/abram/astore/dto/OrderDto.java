package by.abram.astore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {

    private Long id;

    private LocalDateTime orderDate;

    private String status;

    private BigDecimal totalAmount;

    private Long userId;

    private List<ItemDto> items;
}
