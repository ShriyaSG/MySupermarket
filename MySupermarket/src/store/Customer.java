package store;

import discount.DiscountPlan;
import discount.NormalPlan;
import model.BankCard;

/**
 * Represents a supermarket customer (R2.3).
 */
public class Customer extends User {
    private static int nextId = 1;

    private final int customerId;
    private final String address;
    private DiscountPlan plan;
    private BankCard bankCard;

 // Pending delivery request for the next checkout
    private String pendingDeliveryAddress;
    private String pendingDeliverySlotId;  // R10

    public Customer(String firstName, String surname, String username, String address, String password) {
        super(firstName, surname, username, password);
        this.customerId = nextId++;
        this.address = address;
        this.plan = new NormalPlan();
    }

    // For testing: reset ID counter
    public static void resetIdCounter() { nextId = 1; }

    public int getCustomerId() { return customerId; }
    public String getAddress() { return address; }
    public DiscountPlan getPlan() { return plan; }

    public void setPlan(DiscountPlan plan) {
        if (plan == null) throw new IllegalArgumentException("Plan cannot be null");
        this.plan = plan;
    }

    public BankCard getBankCard() { return bankCard; }

    public void setBankCard(BankCard card) { this.bankCard = card; }

    public boolean hasPendingDelivery() { return pendingDeliveryAddress != null; }

    public String getPendingDeliveryAddress() { return pendingDeliveryAddress; }

    public void setPendingDeliveryAddress(String address) { this.pendingDeliveryAddress = address; }

    public void clearPendingDelivery() {
        this.pendingDeliveryAddress = null;
        this.pendingDeliverySlotId  = null;
    }

    public String getPendingDeliverySlotId()          { return pendingDeliverySlotId; }
    public void setPendingDeliverySlotId(String slotId) { this.pendingDeliverySlotId = slotId; }

    @Override
    public String getRole() { return "Customer"; }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, %s %s, plan=%s, card=%s]",
                customerId, getFirstName(), getSurname(), plan.getName(),
                bankCard != null ? bankCard.getCardNumber() : "none");
    }
}
