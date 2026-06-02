package store;

import bank.POSTerminal;
import bank.TransactionAuthorisationSystem;
import delivery.DeliveryService;
import discount.CategoryPricingPolicy;
import discount.DiscountPlan;
import discount.PlanRegistry;
import inventory.InventoryManager;
import inventory.ManagerAlertObserver;
import model.BankCard;
import model.CartEntry;
import model.Item;

import java.util.*;

/**
 * Facade class coordinating all subsystems of the supermarket checkout system.
 * Acts as the main API consumed by the CLI.
 */
public class SupermarketSystem {

    // --- Core components ---
    private final InventoryManager inventory = new InventoryManager();
    private final PlanRegistry planRegistry = new PlanRegistry();
    private final CategoryPricingPolicy pricingPolicy = new CategoryPricingPolicy();
    private final DeliveryService deliveryService = new DeliveryService();
    private final TransactionAuthorisationSystem tas = new TransactionAuthorisationSystem();
    private final POSTerminal pos = new POSTerminal(tas, "POS-001");

    // --- User registry ---
    private final Map<String, User> users = new LinkedHashMap<>();

    // --- Active checkout session ---
    private CheckoutSession currentSession = null;

    // --- Revenue tracking ---
    private double totalRevenue = 0.0;

    // ---- Initialisation -----------------------------------------------------

    public SupermarketSystem() {
        // Default manager
        users.put("ceo", new Manager("CEO", "Admin", "ceo", "123456789"));
        // Attach low-stock observer
        inventory.addObserver(new ManagerAlertObserver("Manager"));
        inventory.addObserver(new ManagerAlertObserver("Supplier"));
    }

    /**
     * Loads the default configuration (command: setup).
     */
    public void setup() {
        // Default item categories and items
        addItemInternal("apple",    "fruit-and-vegetables", 0.30, 0.20, 100);
        addItemInternal("banana",   "fruit-and-vegetables", 0.25, 0.15, 80);
        addItemInternal("tomato",   "fruit-and-vegetables", 0.90, 0.25, 100);
        addItemInternal("milk",     "dairy",                1.20, 1.00, 50);
        addItemInternal("yogurt",   "dairy",                0.80, 0.15, 40);
        addItemInternal("cheese",   "dairy",                3.50, 0.25, 30);
        addItemInternal("steak",    "meat",                12.50, 0.50, 10);
        addItemInternal("chicken",  "meat",                 8.00, 1.00, 20);

        // Default test bank cards
        registerCardInternal("4242424242424242", "1234", 500.0);
        registerCardInternal("1111222233334444", "0000", 200.0);

        // Default cashier and customer
        users.put("cashier1", new Cashier("Default", "Cashier", "cashier1", "cashpwd"));
        Customer defaultCustomer = new Customer("Default", "Customer", "customer1", "1 Main Street", "custpwd");
        defaultCustomer.setBankCard(tas.getCard("4242424242424242"));
        users.put("customer1", defaultCustomer);

        System.out.println("[SETUP] Default configuration loaded.");
    }

    // ---- User management ----------------------------------------------------

    public void registerCashier(String firstName, String lastName, String username, String password) {
        if (users.containsKey(username))
            throw new IllegalArgumentException("Username already exists: " + username);
        users.put(username, new Cashier(firstName, lastName, username, password));
        System.out.printf("[INFO] Cashier registered: %s %s (username: %s)%n", firstName, lastName, username);
    }

    public void registerCustomer(String firstName, String lastName, String username, String address, String password) {
        if (users.containsKey(username))
            throw new IllegalArgumentException("Username already exists: " + username);
        Customer c = new Customer(firstName, lastName, username, address, password);
        // Auto-generate a test bank card for the customer
        String cardNumber = String.format("9%015d", c.getCustomerId());
        BankCard card = new BankCard(cardNumber, "0000", 1000.0);
        tas.registerCard(card);
        c.setBankCard(card);
        users.put(username, c);
        System.out.printf("[INFO] Customer registered: %s %s (username: %s, card: %s)%n",
                firstName, lastName, username, cardNumber);
    }

    public User getUser(String username) { return users.get(username); }

    public boolean authenticate(String username, String password) {
        User u = users.get(username);
        return u != null && u.checkPassword(password);
    }

    // ---- Catalogue management -----------------------------------------------

    public void addItem(String name, String category, double unitPrice, double weightKg, int initialStock) {
        Item item = new Item(name, category, unitPrice, weightKg, initialStock);
        inventory.addItem(item);
        System.out.printf("[INFO] Item added: %s (category: %s, price: %.2f EUR, stock: %d)%n",
                name, category, unitPrice, initialStock);
    }

    private void addItemInternal(String name, String category, double unitPrice, double weightKg, int stock) {
        inventory.addItem(new Item(name, category, unitPrice, weightKg, stock));
    }

    private void registerCardInternal(String number, String pin, double balance) {
        tas.registerCard(new BankCard(number, pin, balance));
    }

    public void restock(String itemName, int quantity) {
        inventory.restock(itemName, quantity);
        System.out.printf("[INFO] Restocked '%s' +%d units.%n", itemName, quantity);
    }

    public void setCategoryDiscount(String category, double discountPercent) {
        pricingPolicy.setDiscount(category, discountPercent);
        System.out.printf("[INFO] Category '%s' discount set to %.0f%%.%n", category, discountPercent);
    }

    // ---- Plan management ----------------------------------------------------

    public void subscribeToPlan(Customer customer, String planName) {
        DiscountPlan plan = planRegistry.get(planName);
        if (plan == null) throw new IllegalArgumentException("Unknown plan: " + planName);
        // Charge the annual fee
        double fee = plan.annualFee();
        if (fee > 0) {
            BankCard card = customer.getBankCard();
            if (card == null) throw new IllegalStateException("Customer has no bank card to charge.");
            if (!card.hasSufficientFunds(fee))
                throw new IllegalStateException(String.format(
                        "Insufficient funds to subscribe to plan '%s' (fee: %.2f EUR).", planName, fee));
            card.debit(fee);
            System.out.printf("[INFO] Annual fee of %.2f EUR charged for plan '%s'.%n", fee, planName);
        }
        customer.setPlan(plan);
        System.out.printf("[INFO] %s subscribed to plan '%s'.%n", customer.getFullName(), planName);
    }

    public PlanRegistry getPlanRegistry() { return planRegistry; }

    // ---- Delivery -----------------------------------------------------------

    public void requestDelivery(Customer customer, String address) {
        customer.setPendingDeliveryAddress(address);
        System.out.printf("[INFO] Delivery request registered for %s at address: %s%n",
                customer.getFullName(), address);
    }

    /** R10 — show available slots for a given cart weight */
    public void showDeliverySlots(double weightKg) {
        var available = deliveryService.getAvailableSlots(weightKg);
        if (available.isEmpty()) {
            System.out.println("[INFO] No delivery slots available for this weight.");
            return;
        }
        System.out.println("Available delivery slots:");
        System.out.printf("  %-6s  %-12s  %-12s  %-13s  %s%n",
            "ID", "Start", "End", "Type", "Remaining capacity");
        for (var slot : available) {
            System.out.printf("  %-6s  %-12s  %-12s  %-13s  %.0f kg%n",
                slot.getSlotId(), slot.getStartTime(), slot.getEndTime(),
                slot.getType(), slot.getRemainingCapacityKg());
        }
    }

    /** R10 — customer picks a slot */
    public void selectDeliverySlot(Customer customer, String slotId) {
        var slot = deliveryService.findSlot(slotId);
        if (slot == null)
            throw new IllegalArgumentException("Unknown slot ID: " + slotId
                + ". Use showSlots to see available slots.");
        customer.setPendingDeliverySlotId(slotId);
        System.out.printf("[INFO] Delivery slot %s (%s-%s, %s) selected for %s.%n",
            slotId, slot.getStartTime(), slot.getEndTime(),
            slot.getType(), customer.getFullName());
    }

    // ---- Checkout session ---------------------------------------------------

    public CheckoutSession startCheckout(Customer customer) {
        if (currentSession != null && !currentSession.isClosed())
            throw new IllegalStateException("A checkout session is already in progress. Please finish it first.");
        currentSession = new CheckoutSession(customer, pricingPolicy, deliveryService,customer.getPendingDeliverySlotId());
        System.out.printf("[INFO] Checkout session started for customer: %s (plan: %s)%n",
                customer.getFullName(), customer.getPlan().getName());
        if (currentSession.getCustomer().hasPendingDelivery()) {
            System.out.println("[INFO] Home delivery requested.");
        }
        return currentSession;
    }

    public void scanItem(String itemName, int quantity) {
        requireOpenSession();
        Item item = inventory.getItem(itemName);
        if (item == null) throw new IllegalArgumentException("Unknown item: " + itemName);
        if (item.getStock() < quantity)
            throw new IllegalStateException(String.format(
                    "Insufficient stock for '%s': requested %d, available %d.", itemName, quantity, item.getStock()));
        currentSession.addItem(item, quantity);
        System.out.printf("  Scanned: %-20s x%3d  @ %.2f EUR%n", item.getName(), quantity, item.getUnitPrice());
    }

    public double computeBill() {
        requireOpenSession();
        return currentSession.computeBill();
    }

    // ---- Payment ------------------------------------------------------------

    public void simulatePayment(String outcome) {
        POSTerminal.PaymentResult result = switch (outcome.toUpperCase()) {
            case "SUCCESS"            -> POSTerminal.PaymentResult.SUCCESS;
            case "INSUFFICIENT_FUNDS" -> POSTerminal.PaymentResult.INSUFFICIENT_FUNDS;
            case "PIN_WRONG"          -> POSTerminal.PaymentResult.PIN_WRONG;
            case "AUTH_DENIED"        -> POSTerminal.PaymentResult.AUTH_DENIED;
            default -> throw new IllegalArgumentException("Unknown simulation outcome: " + outcome);
        };
        pos.setSimulatedOutcome(result);
        System.out.printf("[INFO] Next payment will be simulated as: %s%n", result);
    }

    public boolean pay(String cardNumber, String pin) {
        requireOpenSession();
        double amount = currentSession.getLastComputedBill();
        if (amount < 0)
            throw new IllegalStateException("Please run computeBill before paying.");

        System.out.printf("[POS] Processing payment of %.2f EUR with card %s...%n", amount, cardNumber);
        POSTerminal.PaymentResult result = pos.pay(cardNumber, pin, amount);

        switch (result) {
            case SUCCESS -> {
                System.out.println("[POS] Payment AUTHORISED. Receipt printed.");
                // Decrement inventory, possibly triggering low-stock alerts
                for (CartEntry entry : currentSession.getCart()) {
                    inventory.decrementStock(entry.getItem(), entry.getQuantity());
                }
                totalRevenue += amount;
                currentSession.close();
                return true;
            }
            case PIN_WRONG         -> System.out.println("[POS] Payment REFUSED: incorrect PIN.");
            case CARD_NOT_FOUND    -> System.out.println("[POS] Payment REFUSED: card not recognised.");
            case INSUFFICIENT_FUNDS-> System.out.println("[POS] Payment REFUSED: insufficient funds.");
            case AUTH_DENIED       -> System.out.println("[POS] Payment REFUSED: authorisation denied by bank.");
            case CONNECTION_ERROR  -> System.out.println("[POS] Payment ERROR: could not connect to TAS.");
        }
        return false;
    }

    // ---- Reports ------------------------------------------------------------

    public void showInventory() {
        System.out.println(inventory.inventoryReport());
    }

    public void showRevenue() {
        System.out.printf("Total revenue: %.2f EUR%n", totalRevenue);
    }

    // ---- Accessors for CLI --------------------------------------------------

    public InventoryManager getInventory() { return inventory; }
    public CheckoutSession getCurrentSession() { return currentSession; }
    public double getTotalRevenue() { return totalRevenue; }

    // ---- Helpers ------------------------------------------------------------

    private void requireOpenSession() {
        if (currentSession == null || currentSession.isClosed())
            throw new IllegalStateException("No active checkout session. Use startCheckout first.");
    }
}
