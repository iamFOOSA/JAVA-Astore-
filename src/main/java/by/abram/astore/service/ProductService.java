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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collection;
import java.util.List;

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
        for (Item item : items) {
            item.setProduct(null);
        }
        itemRepository.saveAll(items);

        productRepository.deleteById(id);

        productCacheService.invalidateCache();
    }

    public ProductDto saveWithoutTransaction(ProductDto dto, boolean throwException) {
        Product product = productMapper.toEntity(dto);
        productRepository.save(product);

        if (throwException) {
            throw new IllegalArgumentException("Ошибка! Нет транзакции, продукт останется в БД. ");
        }

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        productCacheService.invalidateCache();

        return productMapper.toDto(product);
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

    private List<Category> findExistingCategories(Collection<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }
        return categoryRepository.findByNameIn(categoryNames.stream().toList());
    }
}