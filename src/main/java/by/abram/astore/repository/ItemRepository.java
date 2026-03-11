package by.abram.astore.repository;

import by.abram.astore.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByProductId(Long productId);
}