package by.abram.astore;

import by.abram.astore.dto.AsyncTaskReportSummary;
import by.abram.astore.dto.AsyncTaskStatusResponse;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ProductAsyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAsyncServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void generateReport_ShouldCompleteTaskAndStoreSummary() {
        ProductAsyncService service = new ProductAsyncService(productRepository, Runnable::run);
        Map<String, AsyncTaskStatusResponse> taskStatuses = new ConcurrentHashMap<>();
        String taskId = "task-1";
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(1);
        taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                taskId,
                "CREATED",
                createdAt,
                null,
                null,
                "created",
                null
        ));

        when(productRepository.findAll()).thenReturn(List.of(
                product("Phone", "Tech", "100.00", 3),
                product("Book", "Books", "20.00", 2),
                product("Draft", " ", null, null)
        ));

        AsyncTaskReportSummary summary = service.generateReport(taskId, taskStatuses).join();
        AsyncTaskStatusResponse status = taskStatuses.get(taskId);

        assertEquals("COMPLETED", status.status());
        assertEquals(createdAt, status.createdAt());
        assertNotNull(status.startedAt());
        assertNotNull(status.finishedAt());
        assertEquals(summary, status.reportSummary());
        assertEquals(3, summary.totalProducts());
        assertEquals(2, summary.totalCategories());
        assertEquals(5, summary.totalUnitsInStock());
        assertEquals(new BigDecimal("340.00"), summary.totalInventoryValue());
    }

    private Product product(String name, String categoryName, String price, Integer quantity) {
        Category category = new Category();
        category.setName(categoryName);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price == null ? null : new BigDecimal(price));
        product.setQuantity(quantity);
        product.setCategories(List.of(category));
        return product;
    }
}
