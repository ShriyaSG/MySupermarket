package cli;

import store.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Command-Line User Interface for the Supermarket Checkout System.
 * Supports interactive input and script execution via runTest.
 */
public class CLI {

    private final SupermarketSystem system;
    private User loggedInUser = null;

    public CLI(SupermarketSystem system) {
        this.system = system;
    }

    // =========================================================================
    // Entry point
    // =========================================================================

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=".repeat(60));
        System.out.println("  MySupermarket CLUI — type 'help' for commands");
        System.out.println("=".repeat(60));
        while (scanner.hasNextLine()) {
            System.out.print(loggedInUser == null ? "guest> " : loggedInUser.getUsername() + "> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye.");
                break;
            }
            processLine(line);
        }
        scanner.close();
    }

    // =========================================================================
    // Line processor — parses tokens then dispatches
    // =========================================================================

    public void processLine(String line) {
        if (line.isBlank() || line.startsWith("#")) return;
        String[] tokens = tokenize(line);
        if (tokens.length == 0) return;
        String cmd = tokens[0].toLowerCase();
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        try {
            switch (cmd) {
                case "login"               -> cmdLogin(args);
                case "logout"              -> cmdLogout(args);
                case "setup"               -> cmdSetup(args);
                case "registercashier"     -> cmdRegisterCashier(args);
                case "registercustomer"    -> cmdRegisterCustomer(args);
                case "additem"             -> cmdAddItem(args);
                case "restock"             -> cmdRestock(args);
                case "setcategorydiscount" -> cmdSetCategoryDiscount(args);
                case "subscribetoplan"     -> cmdSubscribeToPlan(args);
                case "startcheckout"       -> cmdStartCheckout(args);
                case "scanitem"            -> cmdScanItem(args);
                case "computebill"         -> cmdComputeBill(args);
                case "requestdelivery"     -> cmdRequestDelivery(args);
                case "showslots"           -> cmdShowSlots(args);
                case "selectslot"          -> cmdSelectSlot(args);
                case "pay"                 -> cmdPay(args);
                case "simulatepayment"     -> cmdSimulatePayment(args);
                case "showinventory"       -> cmdShowInventory(args);
                case "showrevenue"         -> cmdShowRevenue(args);
                case "runtest"             -> cmdRunTest(args);
                case "help"                -> cmdHelp();
                default                    -> System.out.println("[ERROR] Unknown command: '" + cmd + "'. Type 'help' for the list.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // =========================================================================
    // Command implementations
    // =========================================================================

    private void cmdLogin(String[] args) {
        requireArgs("login", args, 2);
        String username = args[0];
        String password = args[1];
        if (!system.authenticate(username, password)) {
            System.out.println("[ERROR] Invalid username or password.");
            return;
        }
        loggedInUser = system.getUser(username);
        System.out.printf("[INFO] Logged in as %s (%s).%n", loggedInUser.getFullName(), loggedInUser.getRole());
    }

    private void cmdLogout(String[] args) {
        requireLoggedIn();
        System.out.printf("[INFO] %s logged out.%n", loggedInUser.getUsername());
        loggedInUser = null;
    }

    private void cmdSetup(String[] args) {
        requireRole(Manager.class, "setup");
        system.setup();
    }

    private void cmdRegisterCashier(String[] args) {
        requireRole(Manager.class, "registerCashier");
        requireArgs("registerCashier", args, 4);
        system.registerCashier(args[0], args[1], args[2], args[3]);
    }

    private void cmdRegisterCustomer(String[] args) {
        requireRole(Manager.class, "registerCustomer");
        requireArgs("registerCustomer", args, 5);
        system.registerCustomer(args[0], args[1], args[2], args[3], args[4]);
    }

    private void cmdAddItem(String[] args) {
        requireRole(Manager.class, "addItem");
        requireArgs("addItem", args, 5);
        String name = args[0];
        String category = args[1];
        double price = parseDouble(args[2], "unitPrice");
        double weight = parseDouble(args[3], "weight");
        int stock = parseInt(args[4], "initialStock");
        system.addItem(name, category, price, weight, stock);
    }

    private void cmdRestock(String[] args) {
        requireRole(Manager.class, "restock");
        requireArgs("restock", args, 2);
        int qty = parseInt(args[1], "quantity");
        system.restock(args[0], qty);
    }

    private void cmdSetCategoryDiscount(String[] args) {
        requireRole(Manager.class, "setCategoryDiscount");
        requireArgs("setCategoryDiscount", args, 2);
        double pct = parseDouble(args[1], "discountPercent");
        system.setCategoryDiscount(args[0], pct);
    }

    private void cmdSubscribeToPlan(String[] args) {
        requireRole(Customer.class, "subscribeToPlan");
        requireArgs("subscribeToPlan", args, 1);
        system.subscribeToPlan((Customer) loggedInUser, args[0]);
    }

    private void cmdStartCheckout(String[] args) {
        requireRole(Cashier.class, "startCheckout");
        requireArgs("startCheckout", args, 1);
        User u = system.getUser(args[0]);
        if (!(u instanceof Customer))
            throw new IllegalArgumentException("No customer found with username: " + args[0]);
        system.startCheckout((Customer) u);
    }

    private void cmdScanItem(String[] args) {
        requireRole(Cashier.class, "scanItem");
        requireArgs("scanItem", args, 2);
        int qty = parseInt(args[1], "quantity");
        system.scanItem(args[0], qty);
    }

    private void cmdComputeBill(String[] args) {
        requireRole(Cashier.class, "computeBill");
        system.computeBill();
    }

    private void cmdRequestDelivery(String[] args) {
        requireRole(Customer.class, "requestDelivery");
        requireArgs("requestDelivery", args, 1);
        system.requestDelivery((Customer) loggedInUser, args[0]);
    }
    
    private void cmdShowSlots(String[] args) {
        requireRole(Customer.class, "showSlots");
        requireArgs("showSlots", args, 1);
        double weightKg = parseDouble(args[0], "weightKg");
        system.showDeliverySlots(weightKg);
    }

    private void cmdSelectSlot(String[] args) {
        requireRole(Customer.class, "selectSlot");
        requireArgs("selectSlot", args, 1);
        system.selectDeliverySlot((Customer) loggedInUser, args[0]);
    }

    private void cmdPay(String[] args) {
        requireRole(Cashier.class, "pay");
        requireArgs("pay", args, 2);
        system.pay(args[0], args[1]);
    }

    private void cmdSimulatePayment(String[] args) {
        requireRole(Cashier.class, "simulatePayment");
        requireArgs("simulatePayment", args, 1);
        system.simulatePayment(args[0]);
    }

    private void cmdShowInventory(String[] args) {
        requireRole(Manager.class, "showInventory");
        system.showInventory();
    }

    private void cmdShowRevenue(String[] args) {
        requireRole(Manager.class, "showRevenue");
        system.showRevenue();
    }

    private void cmdRunTest(String[] args) {
        requireArgs("runTest", args, 1);
        String filename = args[0];
        try {
            List<String> lines = Files.readAllLines(Path.of(filename));
            System.out.println("[runTest] Executing: " + filename);
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#")) continue;
                System.out.println(">> " + line);
                processLine(line);
            }
            System.out.println("[runTest] Finished: " + filename);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read test file: " + filename + " (" + e.getMessage() + ")");
        }
    }

    private void cmdHelp() {
        System.out.println("""
            Available commands:
              login <username> <password>
              logout
              setup
              registerCashier <firstName> <lastName> <username> <password>
              registerCustomer <firstName> <lastName> <username> <address> <password>
              addItem <itemName> <categoryName> <unitPrice> <weight> <initialStock>
              restock <itemName> <quantity>
              setCategoryDiscount <categoryName> <discountPercent>
              subscribeToPlan <planName>
              startCheckout <customerUsername>
              scanItem <itemName> <quantity>
              computeBill
              requestDelivery <address>
              showSlots <estimatedWeightKg>
              selectSlot <slotId>
              pay <cardNumber> <pin>
              simulatePayment <SUCCESS|INSUFFICIENT_FUNDS|PIN_WRONG|AUTH_DENIED>
              showInventory
              showRevenue
              runTest <testScenario-file>
              help
              exit / quit
            """);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Tokenizes a line, supporting double-quoted arguments with spaces. */
    public static String[] tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ' ' && !inQuote) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }

    private void requireLoggedIn() {
        if (loggedInUser == null)
            throw new IllegalStateException("You must be logged in to perform this action.");
    }

    private void requireRole(Class<? extends User> roleClass, String cmd) {
        requireLoggedIn();
        if (!roleClass.isInstance(loggedInUser))
            throw new IllegalStateException(String.format(
                    "Command '%s' requires role: %s. You are logged in as: %s.",
                    cmd, roleClass.getSimpleName(), loggedInUser.getRole()));
    }

    private void requireArgs(String cmd, String[] args, int expected) {
        if (args.length < expected)
            throw new IllegalArgumentException(String.format(
                    "Command '%s' requires %d argument(s), got %d.", cmd, expected, args.length));
    }

    private double parseDouble(String s, String field) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for '" + field + "': " + s);
        }
    }

    private int parseInt(String s, String field) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for '" + field + "': " + s);
        }
    }

    // ---- Accessors for tests -----------------------------------------------
    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User u) { loggedInUser = u; }
}
