package app.models

import org.scalatest.{FlatSpec, Matchers}

class OrderRequestSpec extends FlatSpec with Matchers {

  import models.{OrderRequest, OrderItem}

  it should "serialise to XML and deserialise from XML properly" in {
    val orderItem1 = OrderItem("latte", "skim", "large")
    val orderItem2 = OrderItem("latte", "semi", "small")
    val orderRequest1 = OrderRequest("takeAway", Seq(orderItem1, orderItem2))
    val xml = orderRequest1.toXML
    val orderRequest2 = OrderRequest.fromXML(xml)
    orderRequest2 should be(orderRequest1)
  }
}
