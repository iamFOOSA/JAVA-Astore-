package by.abram.astore.cache;

import by.abram.astore.dto.ProductDto;
import by.abram.astore.mapper.ProductMapper;
import by.abram.astore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private final Map<ProductCacheKey, Page<ProductDto>> cache = new ConcurrentHashMap<>();

    public Page<ProductDto> getProducts(Long userId, String categoryName, int page, int size) {
        ProductCacheKey key = new ProductCacheKey(userId, categoryName, page, size);
        Page<ProductDto> cachedResult = cache.get(key);

        if (cachedResult != null) {
            log.info("КЭШ: Извлекаются данные из памяти для ключа {}", key.hashCode());
            return cachedResult;
        }

        return cache.computeIfAbsent(key, currentKey -> {
            log.info("БД: Данных в кэше нет, вызов из базы");
            return productRepository.findProductsByUserAndCategoryJPQL(
                            userId, categoryName, PageRequest.of(page, size))
                    .map(productMapper::toDto);
        });
    }

    public void invalidateCache() {
        log.warn("ИНВАЛИДАЦИЯ: Кэш полностью очищен.");
        cache.clear();
    }
}
