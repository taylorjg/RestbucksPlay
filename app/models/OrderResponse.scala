package models

case class OrderResponse(location: String,
                         items: Seq[OrderItem],
                         id: Int,
                         status: String,
                         cost: Double) extends OrderBase

object OrderResponse {

  import scala.xml.Node

  def fromXML(node: Node): OrderResponse = {
    OrderResponse(
      (node \ "location").text,
      (node \ "item") map OrderItem.fromXML,
      0,
      (node \ "status").text,
      (node \ "cost").text.toDouble
    )
  }

  implicit class OrderExtensions(orderResponse: OrderResponse) {
    def toXML: Node =
      <order xmlns="http://schemas.restbucks.com" xmlns:dap="http://schemas.restbucks.com/dap">
        <location>{orderResponse.location}</location>
        {orderResponse.items map (_.toXML)}
        <status>{orderResponse.status}</status>
        <cost>{orderResponse.cost}</cost>
      </order>
  }
}
