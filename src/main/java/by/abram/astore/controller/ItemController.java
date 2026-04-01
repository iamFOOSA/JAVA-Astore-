package by.abram.astore.controller;

import by.abram.astore.dto.ItemDto;
import by.abram.astore.service.ItemService;
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

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Позиции заказа", description = "Управление отдельными товарами (позициями) внутри заказа")
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Добавить позицию", description = "Добавляет новый товар в существующий заказ")
    public ResponseEntity<ItemDto> addItemToOrder(@PathVariable Long orderId,
                                                  @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.create(orderId, itemDto);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Получить все позиции", description = "Возвращает список всех позиций заказов")
    public ResponseEntity<Page<ItemDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itemService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти позицию по ID", description = "Возвращает данные о конкретной позиции")
    public ResponseEntity<ItemDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить позицию", description = "Изменяет количество или цену товара в заказе")
    public ResponseEntity<ItemDto> update(@PathVariable Long id,
                                          @Valid @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.update(id, itemDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить позицию", description = "Удаляет товар из заказа")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}