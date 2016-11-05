package modelSerialisation

import org.scalatest.{Matchers, WordSpec}

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
      val elem = ops.LoadXmlWithSchema(xml, "orderRequest.xsd")
      println(s"elem: $elem")
    }
  }

  "deserialisation of invalid XML" should {
    "fail" in {
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
      val elem = ops.LoadXmlWithSchema(xml, "orderRequest.xsd")
      println(s"elem: $elem")
    }
  }
}
