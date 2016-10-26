package app.models

import org.scalatest.{FlatSpec, Matchers}

class OrderResponseSpec extends FlatSpec with Matchers {

  import models.{OrderItem, OrderResponse}

  it should "serialise to XML and deserialise from XML properly" in {
    val orderItem1 = OrderItem("latte", "skim", "large")
    val orderItem2 = OrderItem("latte", "semi", "small")
    val orderResponse1 = OrderResponse("takeAway", Seq(orderItem1, orderItem2), "payment-expected", 2.99)
    val xml = orderResponse1.toXML
    val orderResponse2 = OrderResponse.fromXML(xml)
    orderResponse2 should be(orderResponse1)
  }
}
