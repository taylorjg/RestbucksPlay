package models

import org.joda.time.DateTime

case class Payment(amount: Double,
                   cardHolder: String,
                   cardNumber: String,
                   expiryMonth: Int,
                   expiryYear: Int,
                   paid: DateTime)

object Payment {

  import scala.xml.Node

  def fromXML(node: Node): Payment =
    Payment(
      (node \ "amount").text.toDouble,
      (node \ "cardHolder").text,
      (node \ "cardNumber").text,
      (node \ "expiryMonth").text.toInt,
      (node \ "expiryYear").text.toInt,
      new DateTime((node \ "paid").text))

  implicit class PaymentExtensions(payment: Payment) {
    def toXML: Node =
      <payment>
        <amount>{payment.amount}</amount>
        <cardHolder>{payment.cardHolder}</cardHolder>
        <cardNumber>{payment.cardNumber}</cardNumber>
        <expiryMonth>{payment.expiryMonth}</expiryMonth>
        <expiryYear>{payment.expiryYear}</expiryYear>
        <paid>{payment.paid}</paid>
      </payment>
  }
}
