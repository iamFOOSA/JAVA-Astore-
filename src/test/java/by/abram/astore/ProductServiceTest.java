package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.AsyncTaskCreateResponse;
import by.abram.astore.dto.AsyncTaskReportSummary;
import by.abram.astore.dto.AsyncTaskStatusResponse;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.RaceConditionResponse;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Product;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.CartItemRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.service.ProductAsyncService;
import by.abram.astore.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private ProductAsyncService productAsyncService;

    @InjectMocks
    private ProductService productService;

    @Test
    void startAsyncReportGeneration_ShouldReturnTaskId() {
        when(productAsyncService.generateReport(anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(new AsyncTaskReportSummary(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                )));

        AsyncTaskCreateResponse response = productService.startAsyncReportGeneration();

        assertNotNull(response);
        assertNotNull(response.taskId());
        assertEquals("CREATED", response.status());
        verify(productAsyncService).generateReport(anyString(), anyMap());
    }

    @Test
    void getTaskStatus_ShouldReturnCreatedStatus() {
        when(productAsyncService.generateReport(anyString(), anyMap()))
                .thenReturn(CompletableFuture.completedFuture(new AsyncTaskReportSummary(
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                )));

        AsyncTaskCreateResponse response = productService.startAsyncReportGeneration();
        AsyncTaskStatusResponse status = productService.getTaskStatus(response.taskId());

        assertNotNull(status);
        assertEquals(response.taskId(), status.taskId());
        assertEquals("CREATED", status.status());
        assertEquals(null, status.reportSummary());
    }

    @Test
    void getTaskStatus_ShouldThrow_WhenTaskDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> productService.getTaskStatus("missing-task-id"));
    }

    @Test
    void create_ShouldSaveProductAndReturnDto() {
        ProductDto dto = new ProductDto();
        dto.setCategories(List.of("Tech"));
        Product product = new Product();
        Category category = new Category();

        when(productMapper.toEntity(dto)).thenReturn(product);
        when(categoryRepository.findByNameIn(any())).thenReturn(List.of(category));
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
        assertEquals(1, productService.getTotalProductViews());
        verify(productRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(1L));
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
    void findByCategory_ShouldReturnPageOfDtos() {
        Product product = new Product();
        ProductDto dto = new ProductDto();

        when(productRepository.findByCategoryId(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toDto(product)).thenReturn(dto);

        Page<ProductDto> result = productService.findByCategory(1L, 0, 10);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByCategoryId(anyLong(), any(PageRequest.class));
    }

    @Test
    void findCatalog_ShouldReturnFilteredPageOfDtos() {
        Product product = new Product();
        ProductDto dto = new ProductDto();

        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toDto(product)).thenReturn(dto);

        Page<ProductDto> result = productService.findCatalog(
                1L,
                "phone",
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500),
                0,
                12);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void demonstrateRaceCondition_ShouldKeepSafeCountersConsistent() {
        RaceConditionResponse response = productService.demonstrateRaceCondition(2_000);

        assertEquals(50, response.threads());
        assertEquals(response.expectedTotal(), response.synchronizedCounter());
        assertEquals(response.expectedTotal(), response.atomicCounter());
    }

    @Test
    void getAsyncStats_ShouldGroupStatuses() {
        when(productAsyncService.generateReport(anyString(), anyMap()))
                .thenAnswer(invocation -> {
                    String taskId = invocation.getArgument(0);
                    Map<String, AsyncTaskStatusResponse> statuses = invocation.getArgument(1);
                    statuses.put(taskId, new AsyncTaskStatusResponse(
                            taskId,
                            "COMPLETED",
                            statuses.get(taskId).createdAt(),
                            statuses.get(taskId).createdAt(),
                            statuses.get(taskId).createdAt(),
                            "done",
                            new AsyncTaskReportSummary(1, 1, 1, BigDecimal.ONE)
                    ));
                    return CompletableFuture.completedFuture(new AsyncTaskReportSummary(
                            1,
                            1,
                            1,
                            BigDecimal.ONE
                    ));
                });

        productService.startAsyncReportGeneration();

        Map<String, Long> stats = productService.getAsyncStats();

        assertEquals(1L, stats.get("COMPLETED"));
    }

    @Test
    void searchByJpql_ShouldReturnPage() {
        Product product = new Product();
        ProductDto dto = new ProductDto();

        when(productRepository.findProductsByUserAndCategoryJPQL(anyLong(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toDto(product)).thenReturn(dto);

        Page<ProductDto> result = productService.searchByJpql(1L, "Category", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productMapper).toDto(product);
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
        when(categoryRepository.findByNameIn(any())).thenReturn(List.of(category));
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

        assertThrows(ResourceNotFoundException.class, () -> productService.update(1L, dto));
    }

    @Test
    void delete_ShouldDetachItemsAndRemoveProduct() {
        Item item1 = new Item();
        item1.setProduct(new Product());
        Item item2 = new Item();
        item2.setProduct(new Product());
        List<Item> items = List.of(item1, item2);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByProductId(1L)).thenReturn(items);

        productService.delete(1L);

        assertEquals(null, item1.getProduct());
        assertEquals(null, item2.getProduct());
        verify(itemRepository).saveAll(items);
        verify(cartItemRepository).deleteByProductId(1L);
        verify(productRepository).deleteById(1L);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void delete_ShouldThrow_WhenProductDoesNotExist() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.delete(1L));
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

        assertThrows(IllegalArgumentException.class, () ->
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

        assertThrows(IllegalArgumentException.class, () ->
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
        Product product = new Product();

        dto.setCategories(null);
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        productService.create(dto);

        verify(categoryRepository, never()).findByNameIn(any());
    }

    @Test
    void findExistingCategories_ShouldReturnEmpty_WhenEmptyList() {
        ProductDto dto = new ProductDto();
        Product product = new Product();

        dto.setCategories(Collections.emptyList());
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        productService.create(dto);

        verify(categoryRepository, never()).findByNameIn(any());
    }
}
