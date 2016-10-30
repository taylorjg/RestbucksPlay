package models

case class PaymentRequest(amount: Double,
                          cardHolder: String,
                          cardNumber: String,
                          expiryMonth: Int,
                          expiryYear: Int) extends PaymentBase

object PaymentRequest {

  import scala.xml.Node

  def fromXML(node: Node): PaymentRequest =
    PaymentRequest(
      (node \ "amount").text.toDouble,
      (node \ "cardHolder").text,
      (node \ "cardNumber").text,
      (node \ "expiryMonth").text.toInt,
      (node \ "expiryYear").text.toInt)

  implicit class PaymentExtensions(paymentRequest: PaymentRequest) {
    def toXML: Node =
      <payment>
        <amount>{paymentRequest.amount}</amount>
        <cardHolder>{paymentRequest.cardHolder}</cardHolder>
        <cardNumber>{paymentRequest.cardNumber}</cardNumber>
        <expiryMonth>{paymentRequest.expiryMonth}</expiryMonth>
        <expiryYear>{paymentRequest.expiryYear}</expiryYear>
      </payment>
  }
}
