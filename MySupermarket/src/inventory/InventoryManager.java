package inventory;

import model.Item;
import java.util.*;

/**
 * Manages the product catalogue and live stock counts.
 * Acts as the Observable in the Observer pattern (R9).
 */
public class InventoryManager {
    private final Map<String, Item> catalogue = new LinkedHashMap<>();
    private final List<StockObserver> observers = new ArrayList<>();

    // ---- Observer management ------------------------------------------------

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Item item) {
        for (StockObserver obs : observers) {
            obs.onLowStock(item);
        }
    }

    // ---- Catalogue operations -----------------------------------------------

    public void addItem(Item item) {
        catalogue.put(item.getName().toLowerCase(), item);
    }

    public boolean hasItem(String name) {
        return catalogue.containsKey(name.toLowerCase());
    }

    public Item getItem(String name) {
        return catalogue.get(name.toLowerCase());
    }

    public Collection<Item> allItems() {
        return Collections.unmodifiableCollection(catalogue.values());
    }

    // ---- Stock operations ---------------------------------------------------

    /**
     * Decrements the stock of the given item and notifies observers if below threshold.
     */
    public void decrementStock(Item item, int quantity) {
        item.decrementStock(quantity);
        if (item.isBelowThreshold()) {
            notifyObservers(item);
        }
    }

    public void restock(String itemName, int quantity) {
        Item item = getItem(itemName);
        if (item == null) throw new IllegalArgumentException("Unknown item: " + itemName);
        item.restock(quantity);
    }

    /**
     * Returns a formatted inventory table.
     */
    public String inventoryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s | %-25s | %10s | %8s | %6s | %s%n",
                "Item", "Category", "Unit Price", "Weight", "Stock", "Status"));
        sb.append("-".repeat(90)).append("\n");
        for (Item item : catalogue.values()) {
            sb.append(item).append("\n");
        }
        return sb.toString();
    }
}
