package de.thws.fiw.kotlin.domainmodel

import java.util.UUID

class Money private constructor(val amount: Double, val currency: String) {
    companion object {
        fun euros(amount: Double) = Money(amount, "EUR")
        fun dollars(amount: Double) = Money(amount, "USD")
        fun pounds(amount: Double) = Money(amount, "GBP")
    }
    override fun toString() = "$amount $currency"
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "Ungültige E-Mail-Adresse" }
    }
}

@JvmInline
value class Sender(val value : Email)

@JvmInline
value class Recipient(val value : Email)

@JvmInline
value class TransactionId(val value: UUID)


sealed class PaymentResult {
    object Completed : PaymentResult()
    data class Failed(val reason: String) : PaymentResult()
}

interface PaymentStrategy{
    fun process(transaction: Transaction) : PaymentResult
}
class CreditCardPaymentStrategy : PaymentStrategy{
    override fun process(transaction: Transaction) : PaymentResult =
        PaymentResult.Completed
}
class PayPalPaymentStrategy : PaymentStrategy{
    override fun process(transaction: Transaction): PaymentResult =
        PaymentResult.Failed("Der PayPal-Dienst ist nicht erreichbar!")
}

sealed class TransactionStatus {
    object Pending : TransactionStatus()
    object Completed : TransactionStatus()
    data class Failed(val reason: String) : TransactionStatus()
}

class Transaction private constructor(
    val money: Money,
    val sender: Sender,
    val recipient: Recipient,
    val paymentStrategy: PaymentStrategy,
    val id: TransactionId = TransactionId(UUID.randomUUID()),
    private var _status: TransactionStatus = TransactionStatus.Pending
) {
    val status : TransactionStatus
        get() = _status

    companion object {
        fun byCreditCard(money: Money, sender: Sender, recipient: Recipient) =
            Transaction(money, sender, recipient, CreditCardPaymentStrategy())

        fun byPayPal(money: Money, sender: Sender, recipient: Recipient) =
            Transaction(money, sender, recipient, PayPalPaymentStrategy())
    }

    init {
        require(sender.value != recipient.value) { "Absender und Empfänger dürfen nicht identisch sein!" }
        require(money.amount > 0) { "Der Betrag muss positiv sein!" }
    }

    fun execute() : PaymentResult {
        val result = paymentStrategy.process(this)

        _status = when (result) {
            is PaymentResult.Completed -> TransactionStatus.Completed
            is PaymentResult.Failed -> TransactionStatus.Failed(result.reason)
        }

        return result
    }

    override fun toString() = "Transaction(id=$id, from=${sender.value}, to=${recipient.value}, amount=$money)"

    override fun equals(other: Any?) = other is Transaction && id == other.id

    override fun hashCode() = id.hashCode()

}



