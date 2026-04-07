package by.abram.astore;

import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderEntityTest {

    @Test
    void addItem_ShouldSetBidirectionalRelation() {
        Order order = new Order();
        Item item = new Item();
        item.setPrice(BigDecimal.TEN);
        item.setQuantity(1);

        order.addItem(item);

        assertEquals(1, order.getItems().size());
        assertSame(order, item.getOrder());
    }

    @Test
    void removeItem_ShouldRemoveAndUnsetRelation() {
        Order order = new Order();
        Item item = new Item();
        item.setPrice(BigDecimal.ONE);
        item.setQuantity(2);
        order.addItem(item);

        order.removeItem(item);

        assertTrue(order.getItems().isEmpty());
        assertNull(item.getOrder());
    }

    @Test
    void calculateTotal_ShouldReturnSum() {
        Order order = new Order();
        Item item1 = new Item();
        item1.setPrice(BigDecimal.valueOf(10));
        item1.setQuantity(2);
        Item item2 = new Item();
        item2.setPrice(BigDecimal.valueOf(3));
        item2.setQuantity(5);
        order.addItem(item1);
        order.addItem(item2);

        BigDecimal result = order.calculateTotal();

        assertEquals(BigDecimal.valueOf(35), result);
    }

    @Test
    void calculateTotal_ShouldReturnZero_WhenNoItems() {
        Order order = new Order();

        assertEquals(BigDecimal.ZERO, order.calculateTotal());
    }
}
