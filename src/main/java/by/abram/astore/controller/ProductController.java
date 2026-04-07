package by.abram.astore.controller;

import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.service.ProductService;
import by.abram.astore.cache.ProductCacheService;
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

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Продукты", description = "Управление каталогом товаров")
public class ProductController {

    private final ProductService productService;
    private final ProductCacheService productCacheService;

    @PostMapping
    @Operation(summary = "Создать продукт", description = "Добавляет новый товар в базу данных с валидацией полей")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto productDto) {
        return new ResponseEntity<>(productService.create(productDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти по ID", description = "Возвращает полную информацию о продукте по его идентификатору")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Получить все продукты", description = "Возвращает список всех товаров с поддержкой пагинации")
    public ResponseEntity<Page<ProductDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить продукт", description = "Обновляет данные существующего товара")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.update(id, productDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить продукт", description = "Удаляет товар из базы данных")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/createDemo")
    @Operation(summary = "Демонстрация транзакций", description = "Создает продукт с возможностью симуляции ошибки")
    public ResponseEntity<ProductDto> runDemo(
            @Valid @RequestBody ProductDto dto,
            @RequestParam boolean useTransaction,
            @RequestParam boolean makeError
    ) {
        if (useTransaction) {
            return ResponseEntity.ok().body(productService.saveWithTransaction(dto, makeError));
        } else {
            return ResponseEntity.ok().body(productService.saveWithoutTransaction(dto, makeError));
        }
    }

    @PostMapping("/bulk-import")
    @Operation(summary = "Массовый импорт продуктов", description = "Загружаем список товаров.")
    public ResponseEntity<List<ProductDto>> bulkImport(
            @RequestBody List<ProductDto> productDtos,
            @RequestParam(defaultValue = "true") boolean useTransaction,
            @RequestParam(defaultValue = "false") boolean simulateError) {

        List<ProductDto> result;
        if (useTransaction) {
            result = productService.bulkImportWithTransaction(productDtos, simulateError);
        } else {
            result = productService.bulkImportWithoutTransaction(productDtos, simulateError);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск (кэшированный)", description = "Поиск продуктов по категории с использованием Redis")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productCacheService.getProducts(userId, categoryName, page, size));
    }

    @GetMapping("/search/jpql")
    @Operation(summary = "Поиск через JPQL", description = "Выполняет поиск товаров, используя объектные запросы JPQL")
    public ResponseEntity<Page<ProductDto>> searchJpql(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByJpql(userId, categoryName, page, size));
    }

    @GetMapping("/search/native")
    @Operation(summary = "Поиск через Native SQL", description = "Выполняет прямой SQL запрос к базе данных")
    public ResponseEntity<Page<ProductCategoryDTO>> searchNative(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByNative(userId, categoryName, page, size));
    }
}