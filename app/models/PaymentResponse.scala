package models

import org.joda.time.DateTime

case class PaymentResponse(amount: Double,
                           cardHolder: String,
                           cardNumber: String,
                           expiryMonth: Int,
                           expiryYear: Int,
                           paid: DateTime) extends PaymentBase

object PaymentResponse {

  import scala.xml.Node

  def fromXML(node: Node): PaymentResponse =
    PaymentResponse(
      (node \ "amount").text.toDouble,
      (node \ "cardHolder").text,
      (node \ "cardNumber").text,
      (node \ "expiryMonth").text.toInt,
      (node \ "expiryYear").text.toInt,
      new DateTime((node \ "paid").text))

  implicit class PaymentExtensions(paymentResponse: PaymentResponse) {
    def toXML: Node =
      <payment>
        <amount>{paymentResponse.amount}</amount>
        <cardHolder>{paymentResponse.cardHolder}</cardHolder>
        <cardNumber>{paymentResponse.cardNumber}</cardNumber>
        <expiryMonth>{paymentResponse.expiryMonth}</expiryMonth>
        <expiryYear>{paymentResponse.expiryYear}</expiryYear>
        <paid>{paymentResponse.paid}</paid>
      </payment>
  }
}
