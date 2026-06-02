package store;

/** Represents a supermarket manager. */
public class Manager extends User {
    public Manager(String firstName, String surname, String username, String password) {
        super(firstName, surname, username, password);
    }

    @Override
    public String getRole() { return "Manager"; }
}
