package models

import org.joda.time.DateTime

case class Receipt(amount: Double, paid: DateTime)

object Receipt {

  import scala.xml.Node

  def fromXML(node: Node): Receipt =
    Receipt(
      (node \ "amount").text.toDouble,
      new DateTime((node \ "paid").text))

  implicit class ReceiptExtensions(receipt: Receipt) {
    def toXML: Node =
      <receipt>
        <amount>{receipt.amount}</amount>
        <paid>{receipt.paid}</paid>
      </receipt>
  }
}
