package discount;

/** Normal plan: no annual fee, no discount. */
public class NormalPlan implements DiscountPlan {

    @Override
    public String getName() { return "normal"; }

    @Override
    public double discountMultiplier(double subtotal) { return 1.0; }

    @Override
    public double annualFee() { return 0.0; }

    @Override
    public double deliveryFeeMultiplier() { return 1.0; }
}
