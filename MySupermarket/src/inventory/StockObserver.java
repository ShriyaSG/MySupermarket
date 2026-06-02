package inventory;

import model.Item;

/**
 * Observer interface for low-stock events (R9, Observer Pattern).
 * Any module interested in stock alerts implements this interface.
 */
public interface StockObserver {
    /**
     * Called when an item's stock falls below its threshold.
     * @param item  the item that triggered the alert
     */
    void onLowStock(Item item);
}
