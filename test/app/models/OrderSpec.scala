package app.models

import org.scalatest.{FlatSpec, Matchers}

class OrderSpec extends FlatSpec with Matchers {

  import models.{Order, OrderItem}

  it should "serialise to XML and deserialise from XML properly" in {
    val orderItem1 = OrderItem("latte", "skim", "large")
    val orderItem2 = OrderItem("latte", "semi", "small")
    val order1 = Order(42, "takeAway", Seq(orderItem1, orderItem2), "payment-expected")
    val xml = order1.toXML
    println(s"xml: $xml")
    val order2 = Order.fromXML(xml)
    order2 should be(order1)
  }
}
