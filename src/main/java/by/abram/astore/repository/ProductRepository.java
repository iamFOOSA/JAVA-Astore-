package by.abram.astore.repository;

import by.abram.astore.dto.ProductCategoryDTO;
import by.abram.astore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = { "categories" })
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = { "categories" })
    Page<Product> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "categories" })
    Optional<Product> findById(Long id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN FETCH p.categories c " +
            "WHERE c.name = :categoryName " +
            "AND EXISTS (SELECT i FROM Item i WHERE i.product.id = p.id AND i.order.user.id = :userId)")
    Page<Product> findProductsByUserAndCategoryJPQL(
            @Param("userId") Long userId,
            @Param("categoryName") String categoryName,
            Pageable pageable);

    @Query(value = "SELECT " +
            "  p.id as id, " +
            "  p.name as name, " +
            "  p.description as description, " +
            "  p.price as price, " +
            "  p.quantity as quantity, " +
            "  c.name as categoryName " +
            "FROM products p " +
            "JOIN product_category pc ON p.id = pc.product_id " +
            "JOIN categories c ON c.id = pc.category_id " +
            "WHERE c.name = :categoryName AND p.id IN ( " +
            "  SELECT i.product_id FROM items i " +
            "  JOIN orders o ON o.id = i.order_id " +
            "  WHERE o.user_id = :userId " +
            ")",
            countQuery = "SELECT count(DISTINCT p.id) FROM products p " +
                    "JOIN product_category pc ON p.id = pc.product_id " +
                    "JOIN categories c ON c.id = pc.category_id " +
                    "WHERE c.name = :categoryName AND EXISTS ( " +
                    "  SELECT 1 FROM items i JOIN orders o ON o.id = i.order_id " +
                    "  WHERE i.product_id = p.id AND o.user_id = :userId)",
            nativeQuery = true)
    Page<ProductCategoryDTO> findProductsByUserAndCategoryNative(
            @Param("userId") Long userId,
            @Param("categoryName") String categoryName,
            Pageable pageable);
}
