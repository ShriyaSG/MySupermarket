package store;

import delivery.DeliveryService;
import discount.CategoryPricingPolicy;
import model.CartEntry;
import model.Item;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single checkout session for one customer (cash register scenario).
 * Holds the shopping cart and computes the bill.
 */
public class CheckoutSession {
    private final Customer customer;
    private final List<CartEntry> cart = new ArrayList<>();
    private boolean closed = false;

 // Delivery
    private final boolean hasDelivery;
    private final String deliveryAddress;
    private final String deliverySlotId;   // R10 — null if no slot chosen
    private final DeliveryService deliveryService;

    // Pricing
    private final CategoryPricingPolicy pricingPolicy;

    // Computed on computeBill()
    private double lastComputedBill = -1;

    public CheckoutSession(Customer customer,
            CategoryPricingPolicy pricingPolicy,
            DeliveryService deliveryService,
            String deliverySlotId) {
this.customer = customer;
this.pricingPolicy = pricingPolicy;
this.deliveryService = deliveryService;

// Capture (and consume) any pending delivery request
this.hasDelivery     = customer.hasPendingDelivery();
this.deliveryAddress = customer.getPendingDeliveryAddress();
this.deliverySlotId  = deliverySlotId;
if (hasDelivery) customer.clearPendingDelivery();
}

    public Customer getCustomer() { return customer; }

    public void addItem(Item item, int quantity) {
        if (closed) throw new IllegalStateException("Session is closed");
        cart.add(new CartEntry(item, quantity));
    }

    public List<CartEntry> getCart() { return Collections.unmodifiableList(cart); }

    public boolean isClosed() { return closed; }

    public void close() { closed = true; }

    // ---- Bill computation ---------------------------------------------------

    /**
     * Computes and prints the itemised bill.
     * @return total amount due (including delivery, after all discounts)
     */
    public double computeBill() {
        System.out.println("=".repeat(60));
        System.out.printf("  RECEIPT — %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        System.out.printf("  Customer: %s (plan: %s)%n", customer.getFullName(), customer.getPlan().getName());
        System.out.println("-".repeat(60));

        double rawItemTotal = 0;
        double categoryDiscountedTotal = 0;

        for (CartEntry entry : cart) {
            double catMultiplier = pricingPolicy.multiplierFor(entry.getItem().getCategory());
            double lineRaw = entry.rawSubtotal();
            double lineAfterCat = lineRaw * catMultiplier;
            rawItemTotal += lineRaw;
            categoryDiscountedTotal += lineAfterCat;

            double catPct = pricingPolicy.discountPercentFor(entry.getItem().getCategory());
            String discountNote = (catPct > 0) ? String.format(" [-%s%% cat.]", (int) catPct) : "";
            System.out.printf("  %-20s x%3d @ %6.2f EUR = %7.2f EUR%s%n",
                    entry.getItem().getName(), entry.getQuantity(),
                    entry.getItem().getUnitPrice(), lineAfterCat, discountNote);
        }

        System.out.println("-".repeat(60));
        System.out.printf("  Subtotal (after category discounts): %8.2f EUR%n", categoryDiscountedTotal);

        // Customer plan discount
        double planMultiplier = customer.getPlan().discountMultiplier(categoryDiscountedTotal);
        double planDiscount = categoryDiscountedTotal * (1 - planMultiplier);
        double itemTotalAfterPlan = categoryDiscountedTotal * planMultiplier;

        if (planDiscount > 0) {
            System.out.printf("  Plan discount (%s):               -%7.2f EUR%n",
                    customer.getPlan().getName(), planDiscount);
            System.out.printf("  Items total (after plan):            %8.2f EUR%n", itemTotalAfterPlan);
        }

     // Delivery fee (R8 + R10)
        double deliveryFee = 0;
        if (hasDelivery) {
            double totalWeight = cart.stream().mapToDouble(CartEntry::totalWeight).sum();
            double distance    = deliveryService.estimateDistanceKm(deliveryAddress);
            System.out.printf("  Delivery to %s:%n", deliveryAddress);
            System.out.printf("    Weight: %.2f kg, Distance: %.1f km%n", totalWeight, distance);

            if (deliverySlotId != null) {
                // R10 — slot booked: dynamic pricing applies
                var slot = deliveryService.findSlot(deliverySlotId);
                deliveryFee = deliveryService.bookSlot(
                    deliverySlotId, totalWeight, distance,
                    categoryDiscountedTotal, customer.getPlan());
                System.out.printf("    Slot: %s (%s-%s, %s) — pricing multiplier: x%.2f%n",
                    slot.getSlotId(), slot.getStartTime(), slot.getEndTime(),
                    slot.getType(), slot.getPricingMultiplier());
            } else {
                // Standard delivery without a slot
                double rawDeliveryFee = deliveryService.computeRawFee(
                    totalWeight, distance, categoryDiscountedTotal);
                deliveryFee = rawDeliveryFee * customer.getPlan().deliveryFeeMultiplier();
            }
            System.out.printf("    Delivery fee charged:            %8.2f EUR%n", deliveryFee);
        }

        double total = itemTotalAfterPlan + deliveryFee;
        System.out.println("=".repeat(60));
        System.out.printf("  TOTAL DUE:                           %8.2f EUR%n", total);
        System.out.println("=".repeat(60));

        lastComputedBill = total;
        return total;
    }

    public double getLastComputedBill() { return lastComputedBill; }

    /** Total weight of all items in the cart. */
    public double totalWeight() {
        return cart.stream().mapToDouble(CartEntry::totalWeight).sum();
    }
}
