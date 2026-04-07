package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Product;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
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
    @Mock private ItemRepository itemRepository;
    @Mock private ProductMapper productMapper;
    @Mock private ProductCacheService productCacheService;

    @InjectMocks private ProductService productService;

    @Test
    void create_ShouldSaveProductAndReturnDto() {
        ProductDto dto = new ProductDto();
        dto.setCategories(List.of("Tech"));
        Product product = new Product();
        Category category = new Category();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(categoryRepository.findByNameIn(anyList())).thenReturn(List.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDto result = productService.create(dto);

        assertNotNull(result);
        verify(productRepository).save(product);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void findById_ShouldReturnDto_WhenProductExists() {
        Product product = new Product();
        ProductDto dto = new ProductDto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDto result = productService.findById(1L);

        assertNotNull(result);
        verify(productRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnPageOfDtos() {
        Product product = new Product();
        ProductDto dto = new ProductDto();
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(dto);

        Page<ProductDto> result = productService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(PageRequest.class));
    }

    @Test
    void searchByJpql_ShouldReturnPage() {
        when(productRepository.findProductsByUserAndCategoryJPQL(anyLong(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(new Product())));

        productService.searchByJpql(1L, "Category", 0, 5);
        verify(productMapper).toDto(any());
    }

    @Test
    void searchByNative_ShouldReturnPage() {
        Page<ProductCategoryDTO> expectedPage = new PageImpl<>(Collections.emptyList());
        when(productRepository.findProductsByUserAndCategoryNative(anyLong(), anyString(), any()))
                .thenReturn(expectedPage);

        Page<ProductCategoryDTO> result = productService.searchByNative(1L, "Tech", 0, 10);

        assertNotNull(result);
        verify(productRepository).findProductsByUserAndCategoryNative(anyLong(), anyString(), any());
    }

    @Test
    void update_ShouldUpdateAndReturnDto_WhenProductExists() {
        ProductDto dto = new ProductDto();
        dto.setName("New Name");
        dto.setDescription("New Desc");
        dto.setPrice(BigDecimal.TEN);
        dto.setQuantity(5);
        dto.setCategories(List.of("Tech"));

        Product product = new Product();
        Category category = new Category();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findByNameIn(anyList())).thenReturn(List.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDto result = productService.update(1L, dto);

        assertNotNull(result);
        assertEquals("New Name", product.getName());
        assertEquals("New Desc", product.getDescription());
        assertEquals(BigDecimal.TEN, product.getPrice());
        assertEquals(5, product.getQuantity());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_ShouldThrowException_WhenProductDoesNotExist() {
        ProductDto dto = new ProductDto();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.update(1L, dto));
    }

    @Test
    void delete_ShouldDetachItemsAndRemoveProduct() {
        Item item1 = new Item();
        item1.setProduct(new Product());
        Item item2 = new Item();
        item2.setProduct(new Product());
        List<Item> items = List.of(item1, item2);

        when(itemRepository.findByProductId(1L)).thenReturn(items);

        productService.delete(1L);

        assertNull(item1.getProduct());
        assertNull(item2.getProduct());
        verify(itemRepository).saveAll(items);
        verify(productRepository).deleteById(1L);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void saveWithoutTransaction_ShouldSaveSuccessfully() {
        ProductDto dto = new ProductDto();
        Product product = new Product();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        productService.saveWithoutTransaction(dto, false);

        verify(productRepository).save(product);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void saveWithoutTransaction_ShouldThrowException_WhenFlagIsTrue() {
        ProductDto dto = new ProductDto();
        Product product = new Product();
        when(productMapper.toEntity(dto)).thenReturn(product);

        assertThrows(IllegalStateException.class, () ->
                productService.saveWithoutTransaction(dto, true));
    }

    @Test
    void saveWithTransaction_ShouldSaveSuccessfully() {
        ProductDto dto = new ProductDto();
        Product product = new Product();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        productService.saveWithTransaction(dto, false);

        verify(productRepository).save(product);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void saveWithTransaction_ShouldThrowException_WhenFlagIsTrue() {
        ProductDto dto = new ProductDto();
        Product product = new Product();
        when(productMapper.toEntity(dto)).thenReturn(product);
        assertThrows(IllegalStateException.class, () ->
                productService.saveWithTransaction(dto, true));
    }

    @Test
    void bulkImport_WithOptionalLogic_AndCategories() {
        ProductDto dto = new ProductDto();
        dto.setName("Phone");
        dto.setDescription("   ");
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
        List<ProductDto> dtos = List.of(dto, dto);

        when(productMapper.toEntity(any())).thenReturn(new Product());
        assertThrows(RuntimeException.class, () ->
                productService.bulkImportWithTransaction(dtos, true)
        );
    }

    @Test
    void bulkImportWithoutTransaction_ShouldProcessImport() {
        ProductDto dto = new ProductDto();
        dto.setDescription("Valid Description");
        Product product = new Product();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any())).thenReturn(product);
        when(productMapper.toDto(any())).thenReturn(dto);

        List<ProductDto> result = productService.bulkImportWithoutTransaction(List.of(dto), false);

        assertEquals(1, result.size());
        verify(productRepository).save(any());
    }


    @Test
    void findExistingCategories_ShouldReturnEmpty_WhenNull() {
        ProductDto dto = new ProductDto();
        dto.setCategories(null);
        when(productMapper.toEntity(dto)).thenReturn(new Product());

        productService.create(dto);

        verify(categoryRepository, never()).findByNameIn(any());
    }

    @Test
    void findExistingCategories_ShouldReturnEmpty_WhenEmptyList() {
        ProductDto dto = new ProductDto();
        dto.setCategories(Collections.emptyList());
        when(productMapper.toEntity(dto)).thenReturn(new Product());

        productService.create(dto);

        verify(categoryRepository, never()).findByNameIn(any());
    }
}
