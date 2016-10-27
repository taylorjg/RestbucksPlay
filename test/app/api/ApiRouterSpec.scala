package app.api

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

import scala.concurrent.Future

class ApiRouterSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import hypermedia.{DapLink, OrderTemplate}
  import mocks.MockDatabaseService
  import models.{OrderItem, OrderResponse}
  import org.scalatest.TestData
  import play.api.Application
  import play.api.inject.bind
  import play.api.inject.guice.GuiceApplicationBuilder
  import play.api.mvc.Result
  import play.api.test.FakeRequest
  import play.api.test.Helpers._
  import services.DatabaseService

  import scala.xml.XML

  private val mockDatabaseService = new MockDatabaseService

  override def newAppForTest(testData: TestData): Application = {
    new GuiceApplicationBuilder()
      .overrides(bind[DatabaseService].toInstance(mockDatabaseService))
      .build
  }

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
      mockDatabaseService.reset()
      mockDatabaseService.setNextOrderId(42)
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder)
      val Some(result) = route(app, request)
      checkDapLinks(result, 42)
    }
  }

  "getting an order" should {
    "return the correct order when it exists" in {

      val id = 42
      val orderItem = OrderItem("milk", "latte", "large")
      val orderResponse = OrderResponse("takeAway", Seq(orderItem), id, "payment-expected", 2.99)

      mockDatabaseService.reset()
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setOrderState(OrderTemplate.template, id, "Unpaid")

      val request = FakeRequest("GET", s"/api/order/$id")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      checkDapLinks(result, id)
    }
  }

  private def checkDapLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(5)
    dapLinks must contain (DapLink("self", s"/api/order/$id", None))
    val mediaType = Some("application/vnd.restbucks+xml")
    dapLinks must contain (DapLink("/api/relations/latest", s"/api/order/$id", mediaType))
    dapLinks must contain (DapLink("/api/relations/update", s"/api/order/$id", mediaType))
    dapLinks must contain (DapLink("/api/relations/payment", s"/api/payment/$id", mediaType))
    dapLinks must contain (DapLink("/api/relations/cancel", s"/api/order/$id", mediaType))
  }
}
