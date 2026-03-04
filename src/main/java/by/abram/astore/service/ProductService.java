package by.abram.astore.service;

import by.abram.astore.dto.ProductDto;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByName(String name) {
        return productRepository.findByNameContaining(name).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}
