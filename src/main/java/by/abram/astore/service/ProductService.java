package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.AsyncTaskCreateResponse;
import by.abram.astore.dto.AsyncTaskStatusResponse;
import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.dto.ProductDto;
import by.abram.astore.dto.RaceConditionResponse;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Product;
import by.abram.astore.exception.BusinessLogicException;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int RACE_THREADS = 50;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductAsyncService productAsyncService;

    private final Map<String, AsyncTaskStatusResponse> taskStatuses = new ConcurrentHashMap<>();
    private final AtomicInteger productViewCounter = new AtomicInteger(0);

    public AsyncTaskCreateResponse startAsyncReportGeneration() {
        String taskId = UUID.randomUUID().toString();

        taskStatuses.put(taskId, new AsyncTaskStatusResponse(
                taskId,
                "CREATED",
                LocalDateTime.now(),
                null,
                null,
                "Задача создана и ожидает выполнения",
                null
        ));

        productAsyncService.generateReport(taskId, taskStatuses);

        return new AsyncTaskCreateResponse(taskId, "CREATED");
    }

    public AsyncTaskStatusResponse getTaskStatus(String taskId) {
        AsyncTaskStatusResponse status = taskStatuses.get(taskId);
        if (status == null) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return status;
    }

    public Map<String, Long> getAsyncStats() {
        return taskStatuses.values().stream()
                .collect(Collectors.groupingBy(AsyncTaskStatusResponse::status, Collectors.counting()));
    }

    public RaceConditionResponse demonstrateRaceCondition(int operationsPerThread) {
        if (operationsPerThread <= 0) {
            throw new BusinessLogicException("Количество операций на поток должно быть больше нуля");
        }

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(RACE_THREADS);
        CountDownLatch readyLatch = new CountDownLatch(RACE_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(RACE_THREADS);

        try {
            for (int i = 0; i < RACE_THREADS; i++) {
                executor.submit(() -> {
                    readyLatch.countDown();

                    try {
                        startLatch.await();

                        for (int j = 0; j < operationsPerThread; j++) {
                            unsafeCounter.increment();
                            synchronizedCounter.increment();
                            atomicCounter.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Поток был прерван во время демонстрации race condition", e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            if (!readyLatch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Не удалось подготовить все рабочие потоки");
            }

            startLatch.countDown();

            if (!doneLatch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Не удалось завершить все потоки вовремя");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Потоки были прерваны", e);
        } finally {
            shutdownExecutor(executor);
        }

        int expectedTotal = RACE_THREADS * operationsPerThread;
        int unsafeValue = unsafeCounter.get();
        int synchronizedValue = synchronizedCounter.get();
        int atomicValue = atomicCounter.get();

        return new RaceConditionResponse(
                RACE_THREADS,
                operationsPerThread,
                expectedTotal,
                unsafeValue,
                synchronizedValue,
                atomicValue,
                unsafeValue != expectedTotal
        );
    }

    public int getTotalProductViews() {
        return productViewCounter.get();
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setCategories(findExistingCategories(dto.getCategories()));
        Product saved = productRepository.save(product);
        productCacheService.invalidateCache();
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategories(findExistingCategories(dto.getCategories()));

        Product saved = productRepository.save(product);
        productCacheService.invalidateCache();
        return productMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        List<Item> items = itemRepository.findByProductId(id);
        items.forEach(item -> item.setProduct(null));

        if (!items.isEmpty()) {
            itemRepository.saveAll(items);
        }

        productRepository.deleteById(id);
        productCacheService.invalidateCache();
    }

    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        productViewCounter.incrementAndGet();
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size))
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> searchByJpql(Long userId, String categoryName, int page, int size) {
        return productRepository.findProductsByUserAndCategoryJPQL(userId, categoryName, PageRequest.of(page, size))
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductCategoryDTO> searchByNative(Long userId, String categoryName, int page, int size) {
        return productRepository.findProductsByUserAndCategoryNative(userId, categoryName, PageRequest.of(page, size));
    }

    @Transactional
    public List<ProductDto> bulkImportWithTransaction(List<ProductDto> dtos, boolean simulateError) {
        List<ProductDto> savedDtos = new ArrayList<>();
        int count = 0;

        for (ProductDto dto : dtos) {
            count++;

            Product product = productMapper.toEntity(dto);
            String validDescription = Optional.ofNullable(dto.getDescription())
                    .filter(desc -> !desc.trim().isEmpty())
                    .orElse("Описание отсутствует (добавлено автоматически)");

            product.setDescription(validDescription);
            product.setCategories(findExistingCategories(dto.getCategories()));

            Product saved = productRepository.save(product);

            if (simulateError && count == dtos.size()) {
                throw new RuntimeException("Симуляция сбоя");
            }

            savedDtos.add(productMapper.toDto(saved));
        }

        productCacheService.invalidateCache();
        return savedDtos;
    }

    public ProductDto saveWithoutTransaction(ProductDto dto, boolean simulateError) {
        Product product = productMapper.toEntity(dto);
        product.setCategories(findExistingCategories(dto.getCategories()));

        if (simulateError) {
            throw new IllegalArgumentException("Симуляция ошибки без транзакции");
        }

        Product saved = productRepository.save(product);
        productCacheService.invalidateCache();
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto saveWithTransaction(ProductDto dto, boolean simulateError) {
        Product product = productMapper.toEntity(dto);
        product.setCategories(findExistingCategories(dto.getCategories()));

        if (simulateError) {
            throw new IllegalArgumentException("Симуляция ошибки в транзакции");
        }

        Product saved = productRepository.save(product);
        productCacheService.invalidateCache();
        return productMapper.toDto(saved);
    }

    public List<ProductDto> bulkImportWithoutTransaction(List<ProductDto> dtos, boolean simulateError) {
        List<ProductDto> savedDtos = new ArrayList<>();
        int count = 0;
        boolean productsSaved = false;

        try {
            for (ProductDto dto : dtos) {
                count++;

                Product product = productMapper.toEntity(dto);
                String validDescription = Optional.ofNullable(dto.getDescription())
                        .filter(desc -> !desc.trim().isEmpty())
                        .orElse("Описание отсутствует (добавлено автоматически)");

                product.setDescription(validDescription);
                product.setCategories(findExistingCategories(dto.getCategories()));

                Product saved = productRepository.save(product);
                productsSaved = true;

                if (simulateError && count == dtos.size()) {
                    throw new RuntimeException("Симуляция сбоя без транзакции");
                }

                savedDtos.add(productMapper.toDto(saved));
            }
        } finally {
            if (productsSaved) {
                productCacheService.invalidateCache();
            }
        }

        return savedDtos;
    }

    private List<Category> findExistingCategories(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        List<String> validNames = names.stream()
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();

        if (validNames.isEmpty()) {
            return List.of();
        }

        return categoryRepository.findByNameIn(validNames);
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private static final class UnsafeCounter {
        private int value;

        void increment() {
            value++;
        }

        int get() {
            return value;
        }
    }

    private static final class SynchronizedCounter {
        private int value;

        synchronized void increment() {
            value++;
        }

        synchronized int get() {
            return value;
        }
    }
}
