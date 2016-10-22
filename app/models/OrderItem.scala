package models

case class OrderItem(coffee: String, milk: String, size: String)

object OrderItem {

  import scala.xml.Node

  def fromXML(node: Node): OrderItem =
    OrderItem(
      (node \ "drink").text,
      (node \ "milk").text,
      (node \ "size").text)

  implicit class OrderItemExtensions(orderItem: OrderItem) {
    def toXML: Node =
      <item>
        <drink>{orderItem.coffee}</drink>
        <milk>{orderItem.milk}</milk>
        <size>{orderItem.size}</size>
      </item>
  }
}
