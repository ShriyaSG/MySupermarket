package discount;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages category-level pricing policies (R6, R6b).
 * A discount percent (0–100) can be set per category name.
 * New categories are accepted at runtime (R6b).
 */
public class CategoryPricingPolicy {
    private final Map<String, Double> categoryDiscounts = new HashMap<>();

    /**
     * Sets a discount percentage for a category.
     * @param category      the item category (case-insensitive)
     * @param discountPercent  0 to 100
     */
    public void setDiscount(String category, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100)
            throw new IllegalArgumentException("Discount percent must be in [0, 100]");
        categoryDiscounts.put(category.toLowerCase(), discountPercent);
    }

    /**
     * Returns the discount multiplier for a given category.
     * E.g. 10% discount → 0.90 multiplier.
     */
    public double multiplierFor(String category) {
        Double pct = categoryDiscounts.get(category.toLowerCase());
        return (pct == null) ? 1.0 : (1.0 - pct / 100.0);
    }

    public double discountPercentFor(String category) {
        return categoryDiscounts.getOrDefault(category.toLowerCase(), 0.0);
    }

    public Map<String, Double> getAllDiscounts() {
        return Map.copyOf(categoryDiscounts);
    }
}
