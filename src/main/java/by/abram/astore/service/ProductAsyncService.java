package by.abram.astore.service;

import by.abram.astore.dto.AsyncTaskReportSummary;
import by.abram.astore.dto.AsyncTaskStatusResponse;
import by.abram.astore.entity.Product;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAsyncService {

    private static final long CREATED_VISIBLE_DELAY_MS = 1_200L;
    private static final long IN_PROGRESS_VISIBLE_DELAY_MS = 2_200L;

    private final ProductRepository productRepository;
    @Qualifier("productTaskExecutor")
    private final Executor productTaskExecutor;

    public CompletableFuture<AsyncTaskReportSummary> generateReport(
            String taskId,
            Map<String, AsyncTaskStatusResponse> taskStatuses
    ) {
        return CompletableFuture.supplyAsync(() -> executeReportWorkflow(taskId, taskStatuses), productTaskExecutor);
    }

    private AsyncTaskReportSummary executeReportWorkflow(
            String taskId,
            Map<String, AsyncTaskStatusResponse> taskStatuses
    ) {
        try {
            log.info("Начата асинхронная задача {}", taskId);
            Thread.sleep(CREATED_VISIBLE_DELAY_MS);

            AsyncTaskStatusResponse current = taskStatuses.get(taskId);
            LocalDateTime createdAt = current != null ? current.createdAt() : LocalDateTime.now();
            LocalDateTime startedAt = LocalDateTime.now();

            taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                    taskId,
                    "IN_PROGRESS",
                    createdAt,
                    startedAt,
                    null,
                    "Генерация отчёта выполняется",
                    null
            ));

            List<Product> products = productRepository.findAll();
            AsyncTaskReportSummary reportSummary = buildReport(products);
            Thread.sleep(IN_PROGRESS_VISIBLE_DELAY_MS);

            taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                    taskId,
                    "COMPLETED",
                    createdAt,
                    startedAt,
                    LocalDateTime.now(),
                    "Отчёт успешно сформирован",
                    reportSummary
            ));

            log.info("Асинхронная задача {} успешно завершена", taskId);
            return reportSummary;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            AsyncTaskStatusResponse current = taskStatuses.get(taskId);
            LocalDateTime createdAt = current != null ? current.createdAt() : LocalDateTime.now();
            LocalDateTime startedAt = current != null ? current.startedAt() : null;

            taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                    taskId,
                    "FAILED",
                    createdAt,
                    startedAt,
                    LocalDateTime.now(),
                    "Задача была прервана",
                    null
            ));

            log.error("Асинхронная задача {} была прервана", taskId, e);
            throw new IllegalStateException(e);
        } catch (Exception e) {
            AsyncTaskStatusResponse current = taskStatuses.get(taskId);
            LocalDateTime createdAt = current != null ? current.createdAt() : LocalDateTime.now();
            LocalDateTime startedAt = current != null ? current.startedAt() : null;

            taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                    taskId,
                    "FAILED",
                    createdAt,
                    startedAt,
                    LocalDateTime.now(),
                    e.getMessage(),
                    null
            ));

            log.error("Ошибка в асинхронной задаче {}", taskId, e);
            throw new IllegalStateException(e);
        }
    }

    private AsyncTaskReportSummary buildReport(List<Product> products) {
        long totalUnitsInStock = products.stream()
                .map(Product::getQuantity)
                .filter(quantity -> quantity != null && quantity > 0)
                .mapToLong(Integer::longValue)
                .sum();

        long totalCategories = products.stream()
                .flatMap(product -> product.getCategories().stream())
                .map(category -> category.getName())
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .count();

        BigDecimal totalInventoryValue = products.stream()
                .map(this::calculateProductValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AsyncTaskReportSummary(
                products.size(),
                totalCategories,
                totalUnitsInStock,
                totalInventoryValue
        );
    }

    private BigDecimal calculateProductValue(Product product) {
        if (product.getPrice() == null || product.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        return product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
    }
}
