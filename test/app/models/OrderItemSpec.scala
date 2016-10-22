package app.models

import org.scalatest.{FlatSpec, Matchers}

class OrderItemSpec extends FlatSpec with Matchers {

  import models.OrderItem

  it should "serialise to XML and deserialise from XML properly" in {
    val orderItem1 = OrderItem("latte", "whole", "small")
    val xml = orderItem1.toXML
    val orderItem2 = OrderItem.fromXML(xml)
    orderItem2 should be(orderItem1)
  }
}
