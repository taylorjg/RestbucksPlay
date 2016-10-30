package modelSerialisation

import org.scalatest.{Matchers, WordSpec}

class XmlSerialisationSpec extends WordSpec with Matchers {

  import models._
  import org.joda.time.DateTime

  "serialisation to and from XML" should {

    "work correctly for PaymentRequest" in {
      val paymentRequest1 = PaymentRequest(12.34, "MR BRUCE FORSYTH", "4111111111111111", 10, 2018)
      val xml = paymentRequest1.toXML
      val paymentRequest2 = PaymentRequest.fromXML(xml)
      paymentRequest2 should be(paymentRequest1)
    }

    "work correctly for PaymentResponse" in {
      val paymentResponse1 = PaymentResponse(12.34, "MR BRUCE FORSYTH", "4111111111111111", 10, 2018, DateTime.now)
      val xml = paymentResponse1.toXML
      val paymentResponse2 = PaymentResponse.fromXML(xml)
      paymentResponse2 should be(paymentResponse1)
    }

    "work correctly for Receipt" in {
      val receipt1 = Receipt(12.34, DateTime.now)
      val xml = receipt1.toXML
      val receipt2 = Receipt.fromXML(xml)
      receipt2 should be(receipt1)
    }

    "work correctly for OrderItem" in {
      val orderItem1 = OrderItem("latte", "whole", "small")
      val xml = orderItem1.toXML
      val orderItem2 = OrderItem.fromXML(xml)
      orderItem2 should be(orderItem1)
    }

    "work correctly for OrderRequest" in {
      val orderItem1 = OrderItem("latte", "skim", "large")
      val orderItem2 = OrderItem("latte", "semi", "small")
      val orderRequest1 = OrderRequest("takeAway", Seq(orderItem1, orderItem2))
      val xml = orderRequest1.toXML
      val orderRequest2 = OrderRequest.fromXML(xml)
      orderRequest2 should be(orderRequest1)
    }

    "work correctly for OrderResponse" in {
      val orderItem1 = OrderItem("latte", "skim", "large")
      val orderItem2 = OrderItem("latte", "semi", "small")
      val orderResponse1 = OrderResponse("takeAway", Seq(orderItem1, orderItem2), 0, "payment-expected", 2.99)
      val xml = orderResponse1.toXML
      val orderResponse2 = OrderResponse.fromXML(xml)
      orderResponse2 should be(orderResponse1)
    }
  }
}
