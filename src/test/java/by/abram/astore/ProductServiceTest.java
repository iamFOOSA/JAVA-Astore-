package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ProductCacheService productCacheService;

    @InjectMocks private ProductService productService;

    @Test
    void bulkImport_WithOptionalLogic_AndCategories() {
        ProductDto dto = new ProductDto();
        dto.setName("Phone");
        dto.setDescription("");
        dto.setCategories(List.of("Tech"));

        Product product = new Product();
        Category category = new Category();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(categoryRepository.findByNameIn(any())).thenReturn(List.of(category));
        when(productRepository.save(any())).thenReturn(product);
        when(productMapper.toDto(any())).thenReturn(dto);

        List<ProductDto> result = productService.bulkImportWithTransaction(List.of(dto), false);

        assertEquals(1, result.size());
        assertEquals("Описание отсутствует (добавлено автоматически)", product.getDescription());
        assertFalse(product.getCategories().isEmpty());
    }

    @Test
    void bulkImport_SimulateError_ShouldThrow() {
        ProductDto dto = new ProductDto();
        when(productMapper.toEntity(any())).thenReturn(new Product());

        assertThrows(RuntimeException.class, () ->
                productService.bulkImportWithTransaction(List.of(dto, dto), true));
    }

    @Test
    void searchByJpql_ShouldReturnPage() {
        when(productRepository.findProductsByUserAndCategoryJPQL(anyLong(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(new Product())));

        productService.searchByJpql(1L, "Category", 0, 5);
        verify(productMapper).toDto(any());
    }

    @Test
    void findExistingCategories_ShouldReturnEmpty_WhenNull() {
        ProductDto dto = new ProductDto();
        dto.setCategories(null);
        when(productMapper.toEntity(dto)).thenReturn(new Product());

        productService.create(dto);

        verify(categoryRepository, never()).findByNameIn(any());
    }
}