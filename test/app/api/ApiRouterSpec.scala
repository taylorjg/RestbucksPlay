package app.api

import hypermedia.DapLink
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

class ApiRouterSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import play.api.test.FakeRequest
  import play.api.test.Helpers._
  import scala.xml.XML

  private val simpleOrder =
    <order xmlns="http://schemas.restbucks.com">
      <location>takeAway</location>
      <item>
        <drink>latte</drink>
        <milk>skim</milk>
        <size>large</size>
      </item>
    </order>

  "creating a new order" should {

    "return status code 201" in {
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
    }

    "return a response body containing the expected hypermedia links" in {
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder)
      val Some(result) = route(app, request)
      val responseDoc = XML.loadString(contentAsString(result))
      val dapLinks = responseDoc \ "link" map DapLink.fromXML
      dapLinks.length must be(5)
      dapLinks must contain (DapLink("self", "/api/order/1", None))
      val mediaType = Some("application/vnd.restbucks+xml")
      dapLinks must contain (DapLink("/api/relations/latest", "/api/order/1", mediaType))
      dapLinks must contain (DapLink("/api/relations/update", "/api/order/1", mediaType))
      dapLinks must contain (DapLink("/api/relations/payment", "/api/payment/1", mediaType))
      dapLinks must contain (DapLink("/api/relations/cancel", "/api/order/1", mediaType))
    }
  }
}
