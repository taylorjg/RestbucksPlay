package models

case class Order(location: String, items: Seq[OrderItem], status: String)

object Order {

  import scala.xml.Node

  def fromXML(node: Node): Order = {
    Order(
      (node \ "location").text,
      (node \ "item") map OrderItem.fromXML,
      (node \ "status").text)
  }

  implicit class OrderExtensions(order: Order) {
    def toXML: Node =
      <order xmlns="http://schemas.restbucks.com" xmlns:dap="http://schemas.restbucks.com/dap">
        <location>{order.location}</location>
        {
          order.items map { item => item.toXML }
        }
        <status>{order.status}</status>
      </order>
  }
}
