package by.abram.astore.controller;

import by.abram.astore.dto.ProductDto;
import by.abram.astore.service.ProductService;
import by.abram.astore.Cache.ProductCacheService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductCacheService productCacheService;

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody ProductDto productDto) {
        return new ResponseEntity<>(productService.create(productDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.update(id, productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/createDemo")
    public ResponseEntity<ProductDto> runDemo(
            @RequestBody ProductDto dto,
            @RequestParam boolean useTransaction,
            @RequestParam boolean makeError
    ) {
        if (useTransaction) {
            return ResponseEntity.ok().body(productService.saveWithTransaction(dto, makeError));
        } else {
            return ResponseEntity.ok().body(productService.saveWithoutTransaction(dto, makeError));
        }
    }
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productCacheService.getProducts(userId, categoryName, page, size));
    }

    @GetMapping("/search/jpql")
    public ResponseEntity<Page<ProductDto>> searchJpql(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByJpql(userId, categoryName, page, size));
    }

    @GetMapping("/search/native")
    public ResponseEntity<Page<ProductDto>> searchNative(
            @RequestParam Long userId,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(productService.searchByNative(userId, categoryName, page, size));
    }
}