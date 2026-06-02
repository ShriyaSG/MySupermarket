package store;

import store.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the SupermarketSystem facade.
 */
class SupermarketSystemTest {

    private SupermarketSystem system;

    @BeforeEach
    void setUp() {
        Customer.resetIdCounter();
        system = new SupermarketSystem();
        system.setup();
    }

    // ---- Authentication -----------------------------------------------------

    @Test
    void authenticateManagerSuccess() {
        assertTrue(system.authenticate("ceo", "123456789"));
    }

    @Test
    void authenticateWrongPassword() {
        assertFalse(system.authenticate("ceo", "wrong"));
    }

    // ---- Registration -------------------------------------------------------

    @Test
    void registerCashierAndLogin() {
        system.registerCashier("John", "Doe", "johndoe", "pwd");
        assertTrue(system.authenticate("johndoe", "pwd"));
        assertInstanceOf(Cashier.class, system.getUser("johndoe"));
    }

    @Test
    void registerCustomer() {
        system.registerCustomer("Jane", "Smith", "janesmith", "1 Main St", "pwd");
        User u = system.getUser("janesmith");
        assertInstanceOf(Customer.class, u);
        Customer c = (Customer) u;
        assertNotNull(c.getBankCard());
    }

    @Test
    void registerDuplicateUsernameThrows() {
        system.registerCashier("A", "B", "dup", "pwd");
        assertThrows(IllegalArgumentException.class,
                () -> system.registerCashier("C", "D", "dup", "pwd2"));
    }

    // ---- Catalogue ----------------------------------------------------------

    @Test
    void addAndFindItem() {
        system.addItem("salmon", "fish", 15.0, 0.3, 20);
        assertNotNull(system.getInventory().getItem("salmon"));
    }

    @Test
    void restock() {
        int before = system.getInventory().getItem("apple").getStock();
        system.restock("apple", 50);
        assertEquals(before + 50, system.getInventory().getItem("apple").getStock());
    }

    // ---- Checkout -----------------------------------------------------------

    @Test
    void basicCheckoutAndBill() {
        system.registerCashier("Bob", "B", "bob2", "pwd");
        system.registerCustomer("Alice", "A", "alice2", "addr", "pwd");

        Customer alice = (Customer) system.getUser("alice2");
        system.startCheckout(alice);
        system.scanItem("apple", 5);
        double bill = system.computeBill();
        // 5 * 0.30 = 1.50
        assertEquals(1.50, bill, 0.01);
    }

    @Test
    void checkoutWithPrimePlanDiscount() {
        system.registerCustomer("Prime", "User", "primeuser", "addr", "pwd");
        Customer c = (Customer) system.getUser("primeuser");
        system.subscribeToPlan(c, "prime");

        system.startCheckout(c);
        // Add items worth > 50 EUR to trigger prime discount
        system.scanItem("steak", 5); // 5 * 12.50 = 62.50
        double bill = system.computeBill();
        // 62.50 * 0.80 = 50.00
        assertEquals(50.00, bill, 0.01);
    }

    @Test
    void simulatedFailedThenSuccessPayment() {
        system.registerCustomer("Pay", "User", "payuser", "addr", "pwd");
        Customer c = (Customer) system.getUser("payuser");
        system.startCheckout(c);
        system.scanItem("apple", 1);
        system.computeBill();

        // Fail
        system.simulatePayment("INSUFFICIENT_FUNDS");
        boolean fail = system.pay(c.getBankCard().getCardNumber(), "0000");
        assertFalse(fail);

        // Succeed
        system.simulatePayment("SUCCESS");
        boolean ok = system.pay(c.getBankCard().getCardNumber(), "0000");
        assertTrue(ok);
    }

    @Test
    void revenueAccumulates() {
        system.registerCustomer("Rev", "User", "revuser", "addr", "pwd");
        Customer c = (Customer) system.getUser("revuser");
        system.startCheckout(c);
        system.scanItem("apple", 2); // 0.60 EUR
        system.computeBill();
        system.simulatePayment("SUCCESS");
        system.pay(c.getBankCard().getCardNumber(), "0000");
        assertTrue(system.getTotalRevenue() > 0);
    }

    @Test
    void noSessionThrowsOnScan() {
        assertThrows(IllegalStateException.class, () -> system.scanItem("apple", 1));
    }
}
