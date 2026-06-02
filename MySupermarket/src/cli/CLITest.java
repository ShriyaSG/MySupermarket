package cli;

import cli.CLI;
import store.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CLITest {

    private SupermarketSystem system;
    private CLI cli;

    @BeforeEach
    void setUp() {
        Customer.resetIdCounter();
        system = new SupermarketSystem();
        cli = new CLI(system);
    }

    // ---- Tokenizer ----------------------------------------------------------

    @Test
    void tokenizeSimple() {
        String[] tokens = CLI.tokenize("scanItem apple 3");
        assertArrayEquals(new String[]{"scanItem", "apple", "3"}, tokens);
    }

    @Test
    void tokenizeQuotedArg() {
        String[] tokens = CLI.tokenize("registerCustomer Alice M alice \"12 rue de la Paix\" alicepwd");
        assertEquals(6, tokens.length);
        assertEquals("12 rue de la Paix", tokens[4]);
    }

    @Test
    void tokenizeEmpty() {
        assertEquals(0, CLI.tokenize("").length);
        assertEquals(0, CLI.tokenize("   ").length);
    }

    // ---- Login / Logout -----------------------------------------------------

    @Test
    void loginSuccess() {
        cli.processLine("login ceo 123456789");
        assertNotNull(cli.getLoggedInUser());
        assertEquals("ceo", cli.getLoggedInUser().getUsername());
    }

    @Test
    void loginWrongPassword() {
        cli.processLine("login ceo wrong");
        assertNull(cli.getLoggedInUser());
    }

    @Test
    void logoutClearsUser() {
        cli.processLine("login ceo 123456789");
        cli.processLine("logout");
        assertNull(cli.getLoggedInUser());
    }

    // ---- Role enforcement ---------------------------------------------------

    @Test
    void setupRequiresManager() {
        // Not logged in → error printed, no exception thrown to test
        cli.processLine("setup"); // should print error, not throw
        assertNull(cli.getLoggedInUser());
    }

    @Test
    void setupAsManagerSucceeds() {
        cli.processLine("login ceo 123456789");
        cli.processLine("setup");
        assertNotNull(system.getInventory().getItem("apple"));
    }

    // ---- Comment lines ------------------------------------------------------

    @Test
    void commentLineIgnored() {
        // Should not crash or cause any state change
        cli.processLine("# this is a comment");
        assertNull(cli.getLoggedInUser());
    }

    // ---- Unknown command ----------------------------------------------------

    @Test
    void unknownCommandPrintsError() {
        // Should not throw
        assertDoesNotThrow(() -> cli.processLine("fooBarBaz"));
    }

    // ---- End-to-end mini flow -----------------------------------------------

    @Test
    void miniEndToEnd() {
        cli.processLine("login ceo 123456789");
        cli.processLine("setup");
        cli.processLine("registerCashier Bob D bob bobpwd");
        cli.processLine("registerCustomer Alice M alice addr alicepwd");
        cli.processLine("logout");

        cli.processLine("login bob bobpwd");
        cli.processLine("startCheckout alice");
        cli.processLine("scanItem apple 2");
        cli.processLine("computeBill");
        cli.processLine("simulatePayment SUCCESS");
        cli.processLine("pay 4242424242424242 1234");
        cli.processLine("logout");

        assertNull(cli.getLoggedInUser());
        assertTrue(system.getTotalRevenue() > 0);
    }
}
