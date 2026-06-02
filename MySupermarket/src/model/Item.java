package model;

/**
 * Represents a sellable item in the supermarket catalogue.
 */
public class Item {
    private final String name;
    private final String category;
    private final double unitPrice;  // price per unit or per kg
    private final double weightKg;   // weight per unit in kg
    private int stock;
    private final int lowStockThreshold;

    public Item(String name, String category, double unitPrice, double weightKg, int initialStock) {
        this(name, category, unitPrice, weightKg, initialStock, 5);
    }

    public Item(String name, String category, double unitPrice, double weightKg, int initialStock, int lowStockThreshold) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Item name cannot be blank");
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        if (weightKg < 0) throw new IllegalArgumentException("Weight cannot be negative");
        if (initialStock < 0) throw new IllegalArgumentException("Initial stock cannot be negative");
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
        this.stock = initialStock;
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getUnitPrice() { return unitPrice; }
    public double getWeightKg() { return weightKg; }
    public int getStock() { return stock; }
    public int getLowStockThreshold() { return lowStockThreshold; }

    public boolean isBelowThreshold() { return stock < lowStockThreshold; }

    /**
     * Decrements stock by quantity. Throws if insufficient stock.
     */
    public void decrementStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (stock < quantity) throw new IllegalStateException("Insufficient stock for item: " + name);
        stock -= quantity;
    }

    /**
     * Adds quantity to stock.
     */
    public void restock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        stock += quantity;
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-25s | %6.2f EUR | %5.2f kg | stock: %d%s",
                name, category, unitPrice, weightKg, stock,
                isBelowThreshold() ? " [LOW STOCK]" : "");
    }
}
