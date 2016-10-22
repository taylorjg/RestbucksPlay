package app.models

import org.scalatest.{FlatSpec, Matchers}

class ReceiptSpec extends FlatSpec with Matchers {

  import models.Receipt
  import org.joda.time.DateTime

  it should "serialise to XML and deserialise from XML properly" in {
    val receipt1 = Receipt(12.34, DateTime.now())
    val xml = receipt1.toXML
    val receipt2 = Receipt.fromXML(xml)
    receipt2 should be(receipt1)
  }
}
