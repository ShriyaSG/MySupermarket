package model;

/**
 * Represents a bank card associated with a customer.
 */
public class BankCard {
    private final String cardNumber;
    private final String pin;
    private double balance;

    public BankCard(String cardNumber, String pin, double balance) {
        if (cardNumber == null || cardNumber.isBlank()) throw new IllegalArgumentException("Card number cannot be blank");
        if (pin == null || pin.isBlank()) throw new IllegalArgumentException("PIN cannot be blank");
        if (balance < 0) throw new IllegalArgumentException("Balance cannot be negative");
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public String getCardNumber() { return cardNumber; }

    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public boolean hasSufficientFunds(double amount) {
        return balance >= amount;
    }

    /**
     * Debits the given amount. Throws if insufficient funds.
     */
    public void debit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (!hasSufficientFunds(amount)) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
    }

    public double getBalance() { return balance; }

    @Override
    public String toString() {
        return String.format("Card[%s, balance=%.2f EUR]", cardNumber, balance);
    }
}
