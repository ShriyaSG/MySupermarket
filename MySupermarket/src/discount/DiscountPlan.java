package discount;

/**
 * Strategy interface for customer discount plans (R5, R5b).
 * New plans can be introduced without modifying existing code (Open/Closed Principle).
 */
public interface DiscountPlan {
    String getName();

    /**
     * Computes the discount multiplier to apply to the subtotal.
     * Returns a value in [0,1]; e.g. 0.80 means 20% off.
     *
     * @param subtotal the pre-discount total of bought items (after category discounts)
     */
    double discountMultiplier(double subtotal);

    /** Annual subscription fee in EUR. */
    double annualFee();

    /**
     * Fraction of the delivery cost charged to the customer.
     * 1.0 = full price, 0.5 = 50% off, 0.0 = free.
     */
    double deliveryFeeMultiplier();
}
