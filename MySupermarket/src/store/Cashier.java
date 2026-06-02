package store;

/** Represents a cashier at the checkout. */
public class Cashier extends User {
    public Cashier(String firstName, String surname, String username, String password) {
        super(firstName, surname, username, password);
    }

    @Override
    public String getRole() { return "Cashier"; }
}
