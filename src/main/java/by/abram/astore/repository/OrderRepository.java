package by.abram.astore.repository;

import by.abram.astore.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    List<Order> findAllWithDetails();

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findAll();

    List<Order> findByUserId(Long userId);
}