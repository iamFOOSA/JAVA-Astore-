package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.CategoryDto;
import by.abram.astore.entity.Category;
import by.abram.astore.mapper.CategoryMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
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

        categoryService.create(dto);

        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        Long categoryId = 1L;
        CategoryDto categoryDto = new CategoryDto();
        when(categoryRepository.existsById(categoryId)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> categoryService.update(categoryId, categoryDto));
    }

    @Test
    void update_Success() {
        CategoryDto dto = new CategoryDto();
        Category entity = mock(Category.class);

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(dto);
        categoryService.update(1L, dto);
        verify(entity).setId(1L);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void findAll_Success() {
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(new Category())));
        categoryService.findAll(0, 10);
        verify(categoryMapper).toDto(any());
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
        verify(categoryRepository).findById(id);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> categoryService.findById(1L));
    }

    @Test
    void delete_Success() {
        Long id = 1L;
        categoryService.delete(id);

        verify(categoryRepository).deleteById(id);
        verify(productCacheService).invalidateCache();
    }
}