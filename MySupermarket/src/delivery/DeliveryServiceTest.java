package delivery;

import delivery.DeliveryService;
import discount.NormalPlan;
import discount.PlatinumPlan;
import discount.PrimePlan;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private DeliveryService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryService();
    }

    @Test
    void lightWeightShortDistanceFlatFee() {
        // < 10 kg, <= 30 km → flat 15 EUR
        double fee = service.computeRawFee(5.0, 20.0, 100.0);
        assertEquals(15.0, fee, 1e-9);
    }

    @Test
    void lightWeightLongDistanceExtraCharge() {
        // < 10 kg, 50 km → 15 + 0.50 * (50-30) = 15 + 10 = 25 EUR
        double fee = service.computeRawFee(5.0, 50.0, 100.0);
        assertEquals(25.0, fee, 1e-9);
    }

    @Test
    void mediumWeightSurcharge() {
        // 10-50 kg → flat 15 + 5% of items total
        double fee = service.computeRawFee(20.0, 10.0, 100.0);
        assertEquals(15.0 + 5.0, fee, 1e-9);
    }

    @Test
    void overweightRefused() {
        assertThrows(IllegalStateException.class,
                () -> service.computeRawFee(51.0, 10.0, 100.0));
    }

    @Test
    void primeHalfDeliveryFee() {
        double fee = service.computeFee(5.0, 20.0, 100.0, new PrimePlan());
        assertEquals(7.5, fee, 1e-9); // 15 * 0.5
    }

    @Test
    void platinumFreeDelivery() {
        double fee = service.computeFee(5.0, 20.0, 100.0, new PlatinumPlan());
        assertEquals(0.0, fee, 1e-9);
    }

    @Test
    void normalFullDeliveryFee() {
        double fee = service.computeFee(5.0, 20.0, 100.0, new NormalPlan());
        assertEquals(15.0, fee, 1e-9);
    }
}
