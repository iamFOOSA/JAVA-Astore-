package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.CategoryDto;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.CategoryMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CategoryMapper categoryMapper;
    @Mock private ProductCacheService productCacheService;
    @InjectMocks private CategoryService categoryService;

    @Test
    void create_Success() {
        CategoryDto dto = new CategoryDto();
        Category entity = new Category();
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(dto);

        CategoryDto result = categoryService.create(dto);

        assertNotNull(result);
        verify(categoryRepository).save(entity);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void findById_Success() {
        Long id = 1L;
        Category category = new Category();
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(dto);

        CategoryDto result = categoryService.findById(id);

        assertNotNull(result);
        assertEquals(dto, result);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.findById(id));
    }

    @Test
    void findAll_Success() {
        PageRequest pageable = PageRequest.of(0, 10);
        Category category = new Category();
        CategoryDto dto = new CategoryDto();
        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(pageable)).thenReturn(page);
        when(categoryMapper.toDto(category)).thenReturn(dto);

        Page<CategoryDto> result = categoryService.findAll(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(categoryMapper).toDto(any());
    }

    @Test
    void update_Success() {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");
        dto.setDescription("Updated description");
        Category entity = new Category();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(dto);

        CategoryDto result = categoryService.update(id, dto);

        assertNotNull(result);
        assertEquals("Updated", entity.getName());
        assertEquals("Updated description", entity.getDescription());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.update(id, dto));
    }

    @Test
    void delete_Success() {
        Long id = 1L;
        Category category = new Category();
        Product product = new Product();
        category.getProducts().add(product);
        product.getCategories().add(category);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.delete(id);

        assertFalse(product.getCategories().contains(category));
        verify(productRepository).saveAll(category.getProducts());
        verify(categoryRepository).delete(category);
        verify(productCacheService).invalidateCache();
    }
}
