package discount;

import discount.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DiscountPlanTest {

    // ---- NormalPlan ---------------------------------------------------------
    @Test
    void normalPlanNoDiscount() {
        DiscountPlan plan = new NormalPlan();
        assertEquals(1.0, plan.discountMultiplier(100.0), 1e-9);
        assertEquals(0.0, plan.annualFee(), 1e-9);
        assertEquals(1.0, plan.deliveryFeeMultiplier(), 1e-9);
    }

    // ---- PrimePlan ----------------------------------------------------------
    @Test
    void primePlanBelowThreshold() {
        DiscountPlan plan = new PrimePlan();
        assertEquals(1.0, plan.discountMultiplier(49.99), 1e-9);
    }

    @Test
    void primePlanAtThreshold() {
        DiscountPlan plan = new PrimePlan();
        assertEquals(0.80, plan.discountMultiplier(50.0), 1e-9);
    }

    @Test
    void primePlanAboveThreshold() {
        DiscountPlan plan = new PrimePlan();
        assertEquals(0.80, plan.discountMultiplier(200.0), 1e-9);
    }

    @Test
    void primePlanFeeAndDelivery() {
        DiscountPlan plan = new PrimePlan();
        assertEquals(50.0, plan.annualFee(), 1e-9);
        assertEquals(0.5, plan.deliveryFeeMultiplier(), 1e-9);
    }

    // ---- PlatinumPlan -------------------------------------------------------
    @Test
    void platinumPlanAlwaysDiscounts() {
        DiscountPlan plan = new PlatinumPlan();
        assertEquals(0.70, plan.discountMultiplier(0.0), 1e-9);
        assertEquals(0.70, plan.discountMultiplier(10.0), 1e-9);
        assertEquals(0.70, plan.discountMultiplier(1000.0), 1e-9);
    }

    @Test
    void platinumPlanFreeDelivery() {
        DiscountPlan plan = new PlatinumPlan();
        assertEquals(0.0, plan.deliveryFeeMultiplier(), 1e-9);
        assertEquals(200.0, plan.annualFee(), 1e-9);
    }

    // ---- PlanRegistry -------------------------------------------------------
    @Test
    void registryContainsDefaults() {
        PlanRegistry registry = new PlanRegistry();
        assertNotNull(registry.get("normal"));
        assertNotNull(registry.get("prime"));
        assertNotNull(registry.get("platinum"));
    }

    @Test
    void registryCustomPlan() {
        PlanRegistry registry = new PlanRegistry();
        DiscountPlan custom = new DiscountPlan() {
            public String getName() { return "vip"; }
            public double discountMultiplier(double s) { return 0.50; }
            public double annualFee() { return 500.0; }
            public double deliveryFeeMultiplier() { return 0.0; }
        };
        registry.register(custom);
        assertTrue(registry.contains("vip"));
        assertEquals(0.50, registry.get("vip").discountMultiplier(1), 1e-9);
    }
}
