package models

case class OrderRequest(location: String, items: Seq[OrderItem]) extends OrderBase

object OrderRequest {

  import scala.xml.Node

  def fromXML(node: Node): OrderRequest = {
    OrderRequest(
      (node \ "location").text,
      (node \ "item") map OrderItem.fromXML)
  }

  implicit class OrderExtensions(orderRequest: OrderRequest) {
    def toXML: Node =
      <order xmlns="http://schemas.restbucks.com" xmlns:dap="http://schemas.restbucks.com/dap">
        <location>{orderRequest.location}</location>
        {orderRequest.items map (_.toXML)}
      </order>
  }
}
