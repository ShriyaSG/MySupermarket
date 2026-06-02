package model;

import model.Item;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item("apple", "fruit-and-vegetables", 0.30, 0.20, 10, 3);
    }

    @Test
    void testConstructorValid() {
        assertEquals("apple", item.getName());
        assertEquals("fruit-and-vegetables", item.getCategory());
        assertEquals(0.30, item.getUnitPrice(), 1e-9);
        assertEquals(0.20, item.getWeightKg(), 1e-9);
        assertEquals(10, item.getStock());
    }

    @Test
    void testConstructorInvalidNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Item("x", "cat", -1.0, 1.0, 5));
    }

    @Test
    void testConstructorBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Item("", "cat", 1.0, 1.0, 5));
    }

    @Test
    void testDecrementStock() {
        item.decrementStock(4);
        assertEquals(6, item.getStock());
    }

    @Test
    void testDecrementStockInsufficient() {
        assertThrows(IllegalStateException.class, () -> item.decrementStock(15));
    }

    @Test
    void testDecrementStockZero() {
        assertThrows(IllegalArgumentException.class, () -> item.decrementStock(0));
    }

    @Test
    void testRestock() {
        item.restock(5);
        assertEquals(15, item.getStock());
    }

    @Test
    void testRestockNegative() {
        assertThrows(IllegalArgumentException.class, () -> item.restock(-1));
    }

    @Test
    void testIsBelowThreshold() {
        assertFalse(item.isBelowThreshold()); // stock=10, threshold=3
        item.decrementStock(8);               // stock=2
        assertTrue(item.isBelowThreshold());
    }
}
