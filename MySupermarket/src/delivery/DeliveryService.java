package delivery;

import delivery.DeliverySlot.SlotType;
import discount.DiscountPlan;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes home-delivery charges (R8) and applies plan-based reductions (R8b).
 * Also manages 2-hour delivery time slots with dynamic pricing (R10).
 */
public class DeliveryService {

    private static final double MAX_WEIGHT_KG          = 50.0;
    private static final double LIGHT_WEIGHT_THRESHOLD = 10.0;
    private static final double FLAT_FEE               = 15.0;
    private static final double DISTANCE_THRESHOLD_KM  = 30.0;
    private static final double EXTRA_KM_RATE          = 0.50;
    private static final double HEAVY_SURCHARGE_RATE   = 0.05;

    // R10 — available delivery slots
    private final List<DeliverySlot> slots = new ArrayList<>();

    public DeliveryService() {
        // Pre-load slots for the day (R10)
        slots.add(new DeliverySlot("S1", LocalTime.of(8,  0), SlotType.NORMAL,       200));
        slots.add(new DeliverySlot("S2", LocalTime.of(10, 0), SlotType.PEAK,         200));
        slots.add(new DeliverySlot("S3", LocalTime.of(12, 0), SlotType.PEAK,         200));
        slots.add(new DeliverySlot("S4", LocalTime.of(14, 0), SlotType.NORMAL,       200));
        slots.add(new DeliverySlot("S5", LocalTime.of(16, 0), SlotType.ECO_FRIENDLY, 200));
        slots.add(new DeliverySlot("S6", LocalTime.of(18, 0), SlotType.PEAK,         200));
        slots.add(new DeliverySlot("S7", LocalTime.of(20, 0), SlotType.NORMAL,       200));
    }

    // ── R8 fee rules ─────────────────────────────────────────────────────────

    public double computeRawFee(double totalWeightKg, double distanceKm, double itemsTotal) {
        if (totalWeightKg > MAX_WEIGHT_KG)
            throw new IllegalStateException(String.format(
                "Delivery refused: total weight %.2f kg exceeds maximum of %.0f kg.",
                totalWeightKg, MAX_WEIGHT_KG));

        double fee = FLAT_FEE;
        if (totalWeightKg >= LIGHT_WEIGHT_THRESHOLD) {
            fee += HEAVY_SURCHARGE_RATE * itemsTotal;
        } else {
            if (distanceKm > DISTANCE_THRESHOLD_KM)
                fee += EXTRA_KM_RATE * (distanceKm - DISTANCE_THRESHOLD_KM);
        }
        return fee;
    }

    public double computeFee(double totalWeightKg, double distanceKm,
                             double itemsTotal, DiscountPlan plan) {
        double raw = computeRawFee(totalWeightKg, distanceKm, itemsTotal);
        return raw * plan.deliveryFeeMultiplier();
    }

    public double estimateDistanceKm(String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.isBlank()) return 10.0;
        int charSum = deliveryAddress.chars().sum();
        return 5.0 + (charSum % 40);
    }

    // ── R10 slot management ──────────────────────────────────────────────────

    public List<DeliverySlot> getAvailableSlots(double weightKg) {
        List<DeliverySlot> available = new ArrayList<>();
        for (DeliverySlot slot : slots) {
            if (slot.hasCapacity(weightKg)) available.add(slot);
        }
        return available;
    }

    /**
     * Books a slot by ID and returns the final delivery fee after
     * plan discount AND dynamic slot pricing (R10).
     */
    public double bookSlot(String slotId, double weightKg, double distanceKm,
                           double itemsTotal, DiscountPlan plan) {
        DeliverySlot slot = findSlot(slotId);
        if (slot == null)
            throw new IllegalArgumentException("Unknown slot ID: " + slotId);
        if (!slot.hasCapacity(weightKg))
            throw new IllegalStateException(
                "Slot " + slotId + " is full. Remaining: "
                + slot.getRemainingCapacityKg() + " kg.");

        slot.book(weightKg);

        double rawFee      = computeRawFee(weightKg, distanceKm, itemsTotal);
        double afterPlan   = rawFee * plan.deliveryFeeMultiplier();
        double finalFee    = afterPlan * slot.getPricingMultiplier();
        return finalFee;
    }

    public DeliverySlot findSlot(String slotId) {
        for (DeliverySlot s : slots)
            if (s.getSlotId().equalsIgnoreCase(slotId)) return s;
        return null;
    }

    public List<DeliverySlot> getAllSlots() { return slots; }
}