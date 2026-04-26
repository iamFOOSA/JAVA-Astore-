package by.abram.astore.dto;

import java.math.BigDecimal;

public record AsyncTaskReportSummary(
        long totalProducts,
        long totalCategories,
        long totalUnitsInStock,
        BigDecimal totalInventoryValue
) {
}
