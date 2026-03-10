package by.abram.astore.service;

import by.abram.astore.dto.CategoryDto;
import by.abram.astore.entity.Category;
import by.abram.astore.mapper.CategoryMapper;
import by.abram.astore.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final String exceptionText = "Category not found, id: ";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(exceptionText + id));
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(exceptionText + id);
        }
        Category category = categoryMapper.toEntity(dto);
        category.setId(id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}