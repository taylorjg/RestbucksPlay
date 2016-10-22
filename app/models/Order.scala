package models

case class Order(id: Int, location: String, items: Seq[OrderItem], status: String)

object Order {

  import scala.xml.Node

  def fromXML(node: Node): Order = {
    Order(
      (node \ "id").text.toInt,
      (node \ "location").text,
      (node \ "item") map OrderItem.fromXML,
      (node \ "status").text)
  }

  implicit class OrderExtensions(order: Order) {
    def toXML: Node =
      <order>
        <id>{order.id}</id>
        <location>{order.location}</location>
        {
          order.items map { item => item.toXML }
        }
        <status>{order.status}</status>
      </order>
  }
}
