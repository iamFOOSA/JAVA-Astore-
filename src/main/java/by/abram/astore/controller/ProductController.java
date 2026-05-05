package by.abram.astore.controller;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.AsyncTaskCreateResponse;
import by.abram.astore.dto.AsyncTaskStatusResponse;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.RaceConditionResponse;
import by.abram.astore.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Продукты", description = "Управление каталогом и демонстрация многопоточности")
public class ProductController {

    private final ProductService productService;
    private final ProductCacheService productCacheService;

    @GetMapping("/views/count")
    @Operation(summary = "Счетчик просмотров (Atomic)")
    public ResponseEntity<Integer> getTotalViews() {
        return ResponseEntity.ok(productService.getTotalProductViews());
    }

    @PostMapping("/async/report")
    @Operation(summary = "Запуск асинхронной задачи")
    public ResponseEntity<AsyncTaskCreateResponse> startAsyncTask() {
        return ResponseEntity.accepted().body(productService.startAsyncReportGeneration());
    }

    @GetMapping("/async/report/{taskId}/status")
    @Operation(summary = "Статус задачи")
    public ResponseEntity<AsyncTaskStatusResponse> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(productService.getTaskStatus(taskId));
    }

    @GetMapping("/async/stats")
    @Operation(summary = "Статистика асинхронных задач")
    public ResponseEntity<Map<String, Long>> getAsyncStats() {
        return ResponseEntity.ok(productService.getAsyncStats());
    }

    @GetMapping("/race-condition")
    public ResponseEntity<RaceConditionResponse> testRace(@RequestParam(defaultValue = "2000") int ops) {
        return ResponseEntity.ok(productService.demonstrateRaceCondition(ops));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }

    @GetMapping("/catalog")
    public ResponseEntity<Page<ProductDto>> findCatalog(@RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) String query,
                                                        @RequestParam(required = false) BigDecimal minPrice,
                                                        @RequestParam(required = false) BigDecimal maxPrice,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.findCatalog(categoryId, query, minPrice, maxPrice, page, size));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDto>> findByCategory(@PathVariable Long categoryId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findByCategory(categoryId, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> search(@RequestParam Long userId,
                                                   @RequestParam String categoryName,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productCacheService.getProducts(userId, categoryName, page, size));
    }

    @GetMapping("/search/jpql")
    public ResponseEntity<Page<ProductDto>> searchJpql(@RequestParam Long userId,
                                                       @RequestParam String categoryName,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByJpql(userId, categoryName, page, size));
    }

    @GetMapping("/search/native")
    public ResponseEntity<Page<ProductCategoryDTO>> searchNative(@RequestParam Long userId,
                                                                 @RequestParam String categoryName,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByNative(userId, categoryName, page, size));
    }
}
