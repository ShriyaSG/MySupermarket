package inventory;

import inventory.InventoryManager;
import inventory.StockObserver;
import model.Item;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class InventoryManagerTest {

    private InventoryManager manager;
    private Item item;

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
        item = new Item("steak", "meat", 12.50, 0.50, 5, 3);
        manager.addItem(item);
    }

    @Test
    void addAndGetItem() {
        assertNotNull(manager.getItem("steak"));
        assertEquals("steak", manager.getItem("steak").getName());
    }

    @Test
    void getItemCaseInsensitive() {
        assertNotNull(manager.getItem("STEAK"));
    }

    @Test
    void getUnknownItem() {
        assertNull(manager.getItem("unknown"));
    }

    @Test
    void restock() {
        manager.restock("steak", 10);
        assertEquals(15, manager.getItem("steak").getStock());
    }

    @Test
    void restockUnknownItemThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.restock("ghost", 5));
    }

    @Test
    void observerNotifiedOnLowStock() {
        List<Item> alerts = new ArrayList<>();
        StockObserver obs = alerts::add;
        manager.addObserver(obs);

        // stock=5, threshold=3; decrement by 3 → stock=2 → below threshold
        manager.decrementStock(item, 3);
        assertEquals(1, alerts.size());
        assertEquals("steak", alerts.get(0).getName());
    }

    @Test
    void observerNotNotifiedAboveThreshold() {
        List<Item> alerts = new ArrayList<>();
        manager.addObserver(alerts::add);

        // stock=5, threshold=3; decrement by 1 → stock=4 → still above threshold
        manager.decrementStock(item, 1);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void observerRemoved() {
        List<Item> alerts = new ArrayList<>();
        StockObserver obs = alerts::add;
        manager.addObserver(obs);
        manager.removeObserver(obs);
        manager.decrementStock(item, 5);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void multipleObserversNotified() {
        List<String> log = new ArrayList<>();
        manager.addObserver(i -> log.add("obs1:" + i.getName()));
        manager.addObserver(i -> log.add("obs2:" + i.getName()));
        manager.decrementStock(item, 4); // stock → 1 < threshold
        assertEquals(2, log.size());
    }
}
