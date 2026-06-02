package bank;

import model.BankCard;

/**
 * Point-of-Sale terminal that manages bank card transactions.
 * Communicates with the TransactionAuthorisationSystem (TAS).
 *
 * Supports a simulatePayment override for reproducible testing.
 */
public class POSTerminal {

    public enum PaymentResult {
        SUCCESS, PIN_WRONG, CARD_NOT_FOUND, INSUFFICIENT_FUNDS, AUTH_DENIED, CONNECTION_ERROR
    }

    private final TransactionAuthorisationSystem tas;
    private final String terminalId;

    // Optional simulation override (null = real behaviour)
    private PaymentResult simulatedOutcome = null;

    public POSTerminal(TransactionAuthorisationSystem tas, String terminalId) {
        this.tas = tas;
        this.terminalId = terminalId;
    }

    /**
     * Forces the result of the next pay() call (for testing, command simulatePayment).
     */
    public void setSimulatedOutcome(PaymentResult outcome) {
        this.simulatedOutcome = outcome;
    }

    /**
     * Processes a payment.
     *
     * @param cardNumber  the card to charge
     * @param enteredPin  PIN entered by the customer
     * @param amount      amount to charge
     * @return            the result of the payment attempt
     */
    public PaymentResult pay(String cardNumber, String enteredPin, double amount) {
        // If a simulation override is set, consume it and return immediately.
        if (simulatedOutcome != null) {
            PaymentResult result = simulatedOutcome;
            simulatedOutcome = null;
            printSimulatedResult(result, amount);
            return result;
        }

        // 1. Verify card is registered in TAS
        if (!tas.isCardRegistered(cardNumber)) {
            return PaymentResult.CARD_NOT_FOUND;
        }

        BankCard card = tas.getCard(cardNumber);

        // 2. Verify PIN
        if (!card.checkPin(enteredPin)) {
            return PaymentResult.PIN_WRONG;
        }

        // 3. Open secure connection to TAS
        try {
            tas.openConnection(terminalId);
        } catch (IllegalStateException e) {
            return PaymentResult.CONNECTION_ERROR;
        }

        // 4. Request authorisation
        TransactionAuthorisationSystem.AuthResult auth = tas.authorise(cardNumber, amount);
        tas.closeConnection();

        return switch (auth) {
            case AUTHORISED          -> PaymentResult.SUCCESS;
            case INSUFFICIENT_FUNDS  -> PaymentResult.INSUFFICIENT_FUNDS;
            case CARD_NOT_FOUND      -> PaymentResult.AUTH_DENIED;
        };
    }

    private void printSimulatedResult(PaymentResult result, double amount) {
        System.out.printf("[POS] Simulated payment outcome: %s (amount: %.2f EUR)%n", result, amount);
    }
}
