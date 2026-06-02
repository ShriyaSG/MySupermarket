package model;

/**
 * Represents a line in the shopping cart: an item and its quantity.
 */
public class CartEntry {
    private final Item item;
    private final int quantity;

    public CartEntry(Item item, int quantity) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }

    /** Raw subtotal before any discount. */
    public double rawSubtotal() {
        return item.getUnitPrice() * quantity;
    }

    /** Total weight for this line. */
    public double totalWeight() {
        return item.getWeightKg() * quantity;
    }

    @Override
    public String toString() {
        return String.format("  %-20s x%3d  @ %6.2f EUR  = %7.2f EUR",
                item.getName(), quantity, item.getUnitPrice(), rawSubtotal());
    }
}
