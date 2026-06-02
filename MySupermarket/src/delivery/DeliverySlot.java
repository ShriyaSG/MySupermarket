package delivery;

import java.time.LocalTime;

/**
 * Represents a 2-hour delivery time slot (R10).
 */
public class DeliverySlot {

    public enum SlotType { NORMAL, PEAK, ECO_FRIENDLY }

    private final String slotId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int maxCapacityKg;
    private double currentLoadKg;
    private SlotType type;

    public DeliverySlot(String slotId, LocalTime startTime, SlotType type, int maxCapacityKg) {
        this.slotId        = slotId;
        this.startTime     = startTime;
        this.endTime       = startTime.plusHours(2);
        this.type          = type;
        this.maxCapacityKg = maxCapacityKg;
        this.currentLoadKg = 0;
    }

    public String getSlotId()        { return slotId; }
    public LocalTime getStartTime()  { return startTime; }
    public LocalTime getEndTime()    { return endTime; }
    public SlotType getType()        { return type; }
    public double getCurrentLoadKg() { return currentLoadKg; }
    public double getRemainingCapacityKg() { return maxCapacityKg - currentLoadKg; }

    public boolean hasCapacity(double weightKg) {
        return (currentLoadKg + weightKg) <= maxCapacityKg;
    }

    public void book(double weightKg) {
        if (!hasCapacity(weightKg))
            throw new IllegalStateException(
                "Slot " + slotId + " is overbooked. Remaining capacity: "
                + getRemainingCapacityKg() + " kg.");
        currentLoadKg += weightKg;
    }

    /**
     * Dynamic pricing multiplier (R10):
     *   PEAK        → +30% on top of normal fee
     *   ECO_FRIENDLY → -20% discount
     *   NORMAL      → no change
     */
    public double getPricingMultiplier() {
        return switch (type) {
            case PEAK         -> 1.30;
            case ECO_FRIENDLY -> 0.80;
            case NORMAL       -> 1.00;
        };
    }

    @Override
    public String toString() {
        return String.format("Slot[%s %s-%s type=%s load=%.1f/%.0fkg]",
            slotId, startTime, endTime, type, currentLoadKg, maxCapacityKg);
    }
}