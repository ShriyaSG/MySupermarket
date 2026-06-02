package discount;

/** Platinum plan: 200 EUR/year, 30% discount always (R5), free delivery (R8b). */
public class PlatinumPlan implements DiscountPlan {
    private static final double DISCOUNT = 0.30;
    private static final double ANNUAL_FEE = 200.0;

    @Override
    public String getName() { return "platinum"; }

    @Override
    public double discountMultiplier(double subtotal) { return 1.0 - DISCOUNT; }

    @Override
    public double annualFee() { return ANNUAL_FEE; }

    @Override
    public double deliveryFeeMultiplier() { return 0.0; }
}
