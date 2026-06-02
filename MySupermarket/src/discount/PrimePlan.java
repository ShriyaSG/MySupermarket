package discount;

/** Prime plan: 50 EUR/year, 20% discount when subtotal >= 50 EUR (R5), 50% delivery fee (R8b). */
public class PrimePlan implements DiscountPlan {
    private static final double THRESHOLD = 50.0;
    private static final double DISCOUNT = 0.20;
    private static final double ANNUAL_FEE = 50.0;

    @Override
    public String getName() { return "prime"; }

    @Override
    public double discountMultiplier(double subtotal) {
        return (subtotal >= THRESHOLD) ? (1.0 - DISCOUNT) : 1.0;
    }

    @Override
    public double annualFee() { return ANNUAL_FEE; }

    @Override
    public double deliveryFeeMultiplier() { return 0.5; }
}
