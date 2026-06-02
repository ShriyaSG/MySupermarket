package inventory;

import model.Item;

/**
 * Concrete observer: prints a low-stock alert to the console (R9).
 * Simulates notifying the Manager or Supplier module.
 */
public class ManagerAlertObserver implements StockObserver {
    private final String role;

    public ManagerAlertObserver(String role) {
        this.role = role;
    }

    @Override
    public void onLowStock(Item item) {
        System.out.printf("[ALERT → %s] LOW STOCK: '%s' (category: %s) — only %d unit(s) remaining (threshold: %d).%n",
                role, item.getName(), item.getCategory(), item.getStock(), item.getLowStockThreshold());
    }
}
