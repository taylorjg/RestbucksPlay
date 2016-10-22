package app.models

import org.scalatest.{FlatSpec, Matchers}

class PaymentSpec extends FlatSpec with Matchers {

  import models.Payment
  import org.joda.time.DateTime

  it should "serialise to XML and deserialise from XML properly" in {
    val payment1 = Payment(12.34, "MR BRUCE FORSYTH", "1234123412341234", 10, 2018, DateTime.now())
    val xml = payment1.toXML
    val payment2 = Payment.fromXML(xml)
    payment2 should be(payment1)
  }
}
