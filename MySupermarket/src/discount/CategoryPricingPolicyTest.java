package discount;

import discount.CategoryPricingPolicy;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoryPricingPolicyTest {

    private CategoryPricingPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new CategoryPricingPolicy();
    }

    @Test
    void defaultMultiplierIsOne() {
        assertEquals(1.0, policy.multiplierFor("unknown-category"), 1e-9);
    }

    @Test
    void setAndGetDiscount() {
        policy.setDiscount("fruit-and-vegetables", 10.0);
        assertEquals(0.90, policy.multiplierFor("fruit-and-vegetables"), 1e-9);
        assertEquals(10.0, policy.discountPercentFor("fruit-and-vegetables"), 1e-9);
    }

    @Test
    void caseInsensitive() {
        policy.setDiscount("Dairy", 5.0);
        assertEquals(0.95, policy.multiplierFor("dairy"), 1e-9);
        assertEquals(0.95, policy.multiplierFor("DAIRY"), 1e-9);
    }

    @Test
    void invalidDiscountThrows() {
        assertThrows(IllegalArgumentException.class, () -> policy.setDiscount("cat", -1));
        assertThrows(IllegalArgumentException.class, () -> policy.setDiscount("cat", 101));
    }

    @Test
    void overrideDiscount() {
        policy.setDiscount("meat", 10.0);
        policy.setDiscount("meat", 20.0);
        assertEquals(0.80, policy.multiplierFor("meat"), 1e-9);
    }
}
