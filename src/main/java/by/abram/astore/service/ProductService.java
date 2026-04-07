package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.Item;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product product = productMapper.toEntity(dto);

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        Product savedProduct = productRepository.save(product);

        productCacheService.invalidateCache();

        return productMapper.toDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Product not found id= " + id));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> searchByJpql(Long userId, String categoryName, int page, int size) {
        return productRepository.findProductsByUserAndCategoryJPQL(
                        userId, categoryName, PageRequest.of(page, size))
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductCategoryDTO> searchByNative(Long userId, String categoryName, int page, int size) {
        return productRepository.findProductsByUserAndCategoryNative(
                userId, categoryName, PageRequest.of(page, size));
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found id= " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        Product updatedProduct = productRepository.save(product);

        productCacheService.invalidateCache();

        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void delete(Long id) {
        List<Item> items = itemRepository.findByProductId(id);

        items.stream().forEach(item -> item.setProduct(null));

        itemRepository.saveAll(items);

        productRepository.deleteById(id);

        productCacheService.invalidateCache();
    }

    @Transactional
    public ProductDto saveWithoutTransaction(ProductDto dto, boolean throwException) {
        Product product = productMapper.toEntity(dto);

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        Product savedProduct = productRepository.saveAndFlush(product);

        if (throwException) {
            throw new IllegalArgumentException("Ошибка! Нет транзакции, продукт останется в БД. ");
        }

        productCacheService.invalidateCache();

        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto saveWithTransaction(ProductDto dto, boolean throwException) {
        Product product = productMapper.toEntity(dto);
        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);
        productRepository.save(product);

        if (throwException) {
            throw new IllegalArgumentException("Ошибка! Благодаря @Transactional произойдет rollback. ");
        }

        productCacheService.invalidateCache();

        return productMapper.toDto(product);
    }

    @Transactional
    public List<ProductDto> bulkImportWithTransaction(List<ProductDto> dtos, boolean simulateError) {
        return processBulkImport(dtos, simulateError);
    }

    public List<ProductDto> bulkImportWithoutTransaction(List<ProductDto> dtos, boolean simulateError) {
        return processBulkImport(dtos, simulateError);
    }

    private List<ProductDto> processBulkImport(List<ProductDto> dtos, boolean simulateError) {
        List<ProductDto> savedDtos = new ArrayList<>();
        int count = 0;
        log.info("woshli");
        for (ProductDto dto : dtos) {
            count++;

            Product product = productMapper.toEntity(dto);

            String validDescription = Optional.ofNullable(dto.getDescription())
                    .filter(desc -> !desc.trim().isEmpty())
                    .orElse("Описание отсутствует (добавлено автоматически)");
            product.setDescription(validDescription);

            List<Category> categories = findExistingCategories(dto.getCategories());
            product.setCategories(categories);

            Product saved = productRepository.saveAndFlush(product);
            log.info("SAVED");
            if (simulateError) {
                throw new RuntimeException("Симуляция сбоя на товаре: " + product.getName() + ".");
            }

            savedDtos.add(productMapper.toDto(saved));
        }

        productCacheService.invalidateCache();
        return savedDtos;
    }

    private List<Category> findExistingCategories(Collection<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }

        List<String> validNames = categoryNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .toList();

        return categoryRepository.findByNameIn(validNames);
    }
}