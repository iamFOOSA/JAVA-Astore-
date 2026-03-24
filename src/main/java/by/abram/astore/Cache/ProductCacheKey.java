package by.abram.astore.Cache;

import java.util.Objects;

public class ProductCacheKey {
    private final Long userId;
    private final String categoryName;
    private final int pageNumber;
    private final int pageSize;

    public ProductCacheKey(Long userId, String categoryName, int pageNumber, int pageSize) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductCacheKey that = (ProductCacheKey) o;
        return pageNumber == that.pageNumber &&
                pageSize == that.pageSize &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, categoryName, pageNumber, pageSize);
    }
}