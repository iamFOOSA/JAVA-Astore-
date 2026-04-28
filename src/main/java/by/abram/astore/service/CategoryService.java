package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.CategoryDto;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.CategoryMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductCacheService productCacheService;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Category category = categoryMapper.toEntity(dto);
        Category savedCategory = categoryRepository.save(category);
        productCacheService.invalidateCache();

        return categoryMapper.toDto(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found" + id));
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(int page, int size) {
        return categoryRepository.findAll(PageRequest.of(page, size))
                .map(categoryMapper::toDto);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id" + id));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category updatedCategory = categoryRepository.save(category);
        productCacheService.invalidateCache();

        return categoryMapper.toDto(updatedCategory);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id" + id));

        for (Product product : category.getProducts()) {
            product.getCategories().remove(category);
        }

        productRepository.saveAll(category.getProducts());
        categoryRepository.delete(category);
        productCacheService.invalidateCache();
    }
}
