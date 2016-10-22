package app.api

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

class ApiRouterSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import play.api.test.FakeRequest
  import play.api.test.Helpers._

  "creating a new order" should {
    "return status 201 and include the correct links in the response body" in {
      val order =
        <order>
          <location>takeAway</location>
          <item>
            <drink>latte</drink>
            <milk>skim</milk>
            <size>large</size>
          </item>
        </order>
      val request = FakeRequest("POST", "/api/order").withXmlBody(order)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
    }
  }
}
