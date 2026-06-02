package bank;

import model.BankCard;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulates the Bank Transaction Authorisation System (TAS).
 * Manages registered bank cards and authorises/refuses transactions.
 */
public class TransactionAuthorisationSystem {
    private final Map<String, BankCard> cards = new HashMap<>();
    private boolean connectionOpen = false;
    private String connectedClient = null;

    // ---- Card registry -------------------------------------------------------

    public void registerCard(BankCard card) {
        cards.put(card.getCardNumber(), card);
    }

    public boolean isCardRegistered(String cardNumber) {
        return cards.containsKey(cardNumber);
    }

    public BankCard getCard(String cardNumber) {
        return cards.get(cardNumber);
    }

    // ---- Connection management ----------------------------------------------

    /**
     * Opens a secure connection for a registered client (e.g. a POS terminal).
     */
    public void openConnection(String clientId) {
        if (connectionOpen) throw new IllegalStateException("A connection is already open");
        connectionOpen = true;
        connectedClient = clientId;
    }

    public void closeConnection() {
        connectionOpen = false;
        connectedClient = null;
    }

    public boolean isConnectionOpen() { return connectionOpen; }

    // ---- Authorisation logic ------------------------------------------------

    /**
     * Result of a transaction authorisation attempt.
     */
    public enum AuthResult { AUTHORISED, CARD_NOT_FOUND, INSUFFICIENT_FUNDS }

    /**
     * Attempts to authorise and perform a transaction.
     * The account is only debited on AUTHORISED.
     */
    public AuthResult authorise(String cardNumber, double amount) {
        BankCard card = cards.get(cardNumber);
        if (card == null) return AuthResult.CARD_NOT_FOUND;
        if (!card.hasSufficientFunds(amount)) return AuthResult.INSUFFICIENT_FUNDS;
        card.debit(amount);
        return AuthResult.AUTHORISED;
    }
}
