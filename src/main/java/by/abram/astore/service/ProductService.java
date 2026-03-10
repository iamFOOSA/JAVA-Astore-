package by.abram.astore.service;

import by.abram.astore.dto.ProductDto;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;



    @Transactional
    public ProductDto create(ProductDto dto) {

        Product product = productMapper.toEntity(dto);

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        productRepository.save(product);

        return productMapper.toDto(product);
    }


    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product not found id=" + id));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }


    @Transactional
    public ProductDto update(Long id, ProductDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product not found id=" + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        productRepository.save(product);

        return productMapper.toDto(product);
    }


    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }


    public ProductDto saveWithoutTransaction(ProductDto dto, boolean throwException) {

        Product product = productMapper.toEntity(dto);

        productRepository.save(product);

        if (throwException) {
            throw new RuntimeException(
                    "Ошибка! Нет транзакции, продукт останется в БД."
            );
        }

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        return productMapper.toDto(product);
    }



    @Transactional
    public ProductDto saveWithTransaction(ProductDto dto, boolean throwException) {

        Product product = productMapper.toEntity(dto);

        List<Category> categories = findExistingCategories(dto.getCategories());
        product.setCategories(categories);

        productRepository.save(product);

        if (throwException) {
            throw new RuntimeException(
                    "Ошибка! Благодаря @Transactional произойдет rollback."
            );
        }

        return productMapper.toDto(product);
    }


    private List<Category> findExistingCategories(Collection<String> categoryNames) {

        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }

        return categoryRepository.findByNameIn(categoryNames.stream().toList());
    }
}