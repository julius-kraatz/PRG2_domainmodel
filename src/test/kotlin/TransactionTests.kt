import de.thws.fiw.kotlin.domainmodel.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TransactionTests {

    @Test
    @DisplayName("Money.euros sets given amount and currency EUR")
    fun money_euros(){
        val money = Money.euros(50.0)
        assertEquals(50.0, money.amount)
        assertEquals("EUR", money.currency)
    }

    @Test
    @DisplayName("Money.dollars sets given amount and currency USD")
    fun money_dollars(){
        val money = Money.dollars(123.3)
        assertEquals(123.3, money.amount)
        assertEquals("USD", money.currency)
    }

    @Test
    @DisplayName("Money.pounds sets given amount and currency GBP")
    fun money_pounds(){
        val money = Money.pounds(1.5)
        assertEquals(1.5, money.amount)
        assertEquals("GBP", money.currency)
    }

    @Test
    @DisplayName("Empty Email address is not allowed")
    fun email_empty_not_allowed() {
        assertThrows<IllegalArgumentException> {
            Email("")
        }
    }

    @Test
    @DisplayName("Email address without @ sign is not allowed")
    fun email_without_at_sign_not_allowed(){
        assertThrows<IllegalArgumentException> {
            Email("testATstudy.thws.de")
        }
    }

    @Test
    @DisplayName("Email address with @ sign is allowed")
    fun correct_email_created(){
        val email = Email("test@study.thws.de")
        assertEquals("test@study.thws.de", email.value)
    }

    @Test
    @DisplayName("Transaction with money amount 0 or negative is forbidden")
    fun transaction_money_must_be_positive(){
        val zeroMoney = Money.euros(0.0)
        val negativeMoney = Money.euros(-100.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))

        assertThrows<IllegalArgumentException> {
            Transaction.byCreditCard(zeroMoney, sender, recipient)
        }
        assertThrows<IllegalArgumentException> {
            Transaction.byCreditCard(negativeMoney, sender, recipient)
        }
        assertThrows<IllegalArgumentException> {
            Transaction.byPayPal(zeroMoney, sender, recipient)
        }
        assertThrows<IllegalArgumentException> {
            Transaction.byPayPal(negativeMoney, sender, recipient)
        }
    }

    @Test
    @DisplayName("Transaction with the same sender and recipient is forbidden")
    fun transaction_sender_and_recipient_must_be_different(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test@study.thws.de"))

        assertThrows<IllegalArgumentException> {
            Transaction.byCreditCard(money, sender, recipient)
        }
        assertThrows<IllegalArgumentException> {
            Transaction.byPayPal(money, sender, recipient)
        }
    }

    @Test
    @DisplayName("Transaction status is initially TransactionStatus.Pending")
    fun transaction_status_pending(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))

        val creditCardTransaction = Transaction.byCreditCard(money, sender, recipient)
        val payPalTransaction = Transaction.byPayPal(money, sender, recipient)

        assertEquals(TransactionStatus.Pending, creditCardTransaction.status)
        assertEquals(TransactionStatus.Pending, payPalTransaction.status)
    }

    @Test
    @DisplayName("Harcoded: CreditCardPaymentStrategy.process always succeeds")
    fun credit_card_payment_always_succeeds(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))
        val transaction = Transaction.byCreditCard(money, sender, recipient)

        val result = CreditCardPaymentStrategy().process(transaction)

        assertEquals(PaymentResult.Completed, result)
    }

    @Test
    @DisplayName("Harcoded: PayPalPaymentStrategy.process always fails")
    fun paypal_payment_always_fails(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))
        val transaction = Transaction.byPayPal(money, sender, recipient)

        val result = PayPalPaymentStrategy().process(transaction)

        assert(result is PaymentResult.Failed)
    }

    @Test
    @DisplayName("Transaction status is TransactionStatus.Completed after payment")
    fun transaction_status_completed(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))

        val transaction = Transaction.byCreditCard(money, sender, recipient)
        transaction.execute()

        assertEquals(TransactionStatus.Completed, transaction.status)
    }

    @Test
    @DisplayName("Transaction status is TransactionStatus.Failed after payment fail")
    fun transaction_status_failed(){
        val money = Money.euros(50.0)
        val sender = Sender(Email("test@study.thws.de"))
        val recipient = Recipient(Email("test2@study.thws.de"))

        val transaction = Transaction.byPayPal(money, sender, recipient)
        transaction.execute()

        assert(transaction.status is TransactionStatus.Failed)
    }
}