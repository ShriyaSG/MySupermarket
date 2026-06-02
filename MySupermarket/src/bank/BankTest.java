package bank;

import bank.TransactionAuthorisationSystem;
import model.BankCard;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    private BankCard card;
    private TransactionAuthorisationSystem tas;

    @BeforeEach
    void setUp() {
        card = new BankCard("1234567890123456", "1234", 200.0);
        tas = new TransactionAuthorisationSystem();
        tas.registerCard(card);
    }

    // ---- BankCard -----------------------------------------------------------

    @Test
    void correctPinAccepted() {
        assertTrue(card.checkPin("1234"));
    }

    @Test
    void wrongPinRejected() {
        assertFalse(card.checkPin("0000"));
    }

    @Test
    void sufficientFunds() {
        assertTrue(card.hasSufficientFunds(199.99));
    }

    @Test
    void insufficientFunds() {
        assertFalse(card.hasSufficientFunds(200.01));
    }

    @Test
    void debitReducesBalance() {
        card.debit(50.0);
        assertEquals(150.0, card.getBalance(), 1e-9);
    }

    @Test
    void debitInsufficient() {
        assertThrows(IllegalStateException.class, () -> card.debit(201.0));
    }

    @Test
    void debitZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> card.debit(0));
    }

    // ---- TAS ----------------------------------------------------------------

    @Test
    void authoriseSuccess() {
        var result = tas.authorise("1234567890123456", 50.0);
        assertEquals(TransactionAuthorisationSystem.AuthResult.AUTHORISED, result);
        assertEquals(150.0, card.getBalance(), 1e-9);
    }

    @Test
    void authoriseInsufficientFunds() {
        var result = tas.authorise("1234567890123456", 999.0);
        assertEquals(TransactionAuthorisationSystem.AuthResult.INSUFFICIENT_FUNDS, result);
        assertEquals(200.0, card.getBalance(), 1e-9); // unchanged
    }

    @Test
    void authoriseUnknownCard() {
        var result = tas.authorise("9999999999999999", 10.0);
        assertEquals(TransactionAuthorisationSystem.AuthResult.CARD_NOT_FOUND, result);
    }

    @Test
    void connectionOpenClose() {
        assertFalse(tas.isConnectionOpen());
        tas.openConnection("POS-001");
        assertTrue(tas.isConnectionOpen());
        tas.closeConnection();
        assertFalse(tas.isConnectionOpen());
    }

    @Test
    void doubleOpenConnectionThrows() {
        tas.openConnection("POS-001");
        assertThrows(IllegalStateException.class, () -> tas.openConnection("POS-002"));
        tas.closeConnection();
    }
}
