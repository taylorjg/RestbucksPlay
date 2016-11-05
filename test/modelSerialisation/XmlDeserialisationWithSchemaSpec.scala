package modelSerialisation

import org.scalatest.{Matchers, WordSpec}
import org.xml.sax.SAXParseException

import scala.util._

class XmlDeserialisationWithSchemaSpec extends WordSpec with Matchers {

  "deserialisation of valid XML" should {
    "succeed" in {
      val xml =
        """
          |<order>
          |  <location>takeAway</location>
          |  <item>
          |    <drink>latte</drink>
          |    <milk>skim</milk>
          |    <size>large</size>
          |  </item>
          |</order>
        """.stripMargin
      val tryElem = ops.LoadXmlWithSchema(xml, "schema/orderRequest.xsd")
      tryElem match {
        case Success(_) =>
        case _ => fail("Expected Success(_)")
      }
    }
  }

  "deserialisation of invalid XML" should {
    "fail with SAXParseException" in {
      val xml =
        """
          |<order>
          |  <!-- missing location element -->
          |  <item>
          |    <drink>latte</drink>
          |    <milk>skim</milk>
          |    <size>large</size>
          |  </item>
          |</order>
        """.stripMargin
      val tryElem = ops.LoadXmlWithSchema(xml, "schema/orderRequest.xsd")
      tryElem match {
        case Failure(ex: SAXParseException) =>
        case _ => fail("Expected Failure(SAXParseException)")
      }
    }
  }
}
