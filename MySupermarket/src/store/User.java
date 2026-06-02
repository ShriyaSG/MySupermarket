package store;

/**
 * Base class for all system users (Manager, Cashier, Customer).
 */
public abstract class User {
    private final String firstName;
    private final String surname;
    private final String username;
    private final String password;

    protected User(String firstName, String surname, String username, String password) {
        this.firstName = firstName;
        this.surname = surname;
        this.username = username;
        this.password = password;
    }

    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getUsername() { return username; }
    public String getFullName() { return firstName + " " + surname; }

    public boolean checkPassword(String pwd) { return password.equals(pwd); }

    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("%s[%s %s, username=%s]", getRole(), firstName, surname, username);
    }
}
