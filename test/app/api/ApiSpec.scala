package app.api

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

import scala.concurrent.Future

class ApiSpec extends PlaySpec
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
  import services.{DatabaseService, OrderStatuses}

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
      verifyUnpaidOrderHypermediaLinks(result, 42)
    }
  }

  "getting an order in the Unpaid state" should {
    "return a response body containing the expected hypermedia links" in {

      val id = 42
      val orderItem = OrderItem("milk", "latte", "large")
      val orderResponse = OrderResponse("takeAway", Seq(orderItem), id, OrderStatuses.PaymentExpected, 2.99)

      mockDatabaseService.reset()
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, id, "Unpaid")

      val request = FakeRequest("GET", s"/api/order/$id")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyUnpaidOrderHypermediaLinks(result, id)
    }
  }

  "getting an order in the Preparing state" should {
    "return a response body containing the expected hypermedia links" in {

      val id = 42
      val orderItem = OrderItem("milk", "latte", "large")
      val orderResponse = OrderResponse("takeAway", Seq(orderItem), id, OrderStatuses.Preparing, 2.99)

      mockDatabaseService.reset()
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, id, "Preparing")
      //mockDatabaseService.setResourceState(PaymentTemplate.template, id, "PaymentExpected")

      val request = FakeRequest("GET", s"/api/order/$id")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyPreparingOrderHypermediaLinks(result, id)
    }
  }

  "getting an order in the Ready state" should {
    "return a response body containing the expected hypermedia links" in {

      val id = 42
      val orderItem = OrderItem("milk", "latte", "large")
      val orderResponse = OrderResponse("takeAway", Seq(orderItem), id, OrderStatuses.Ready, 2.99)

      mockDatabaseService.reset()
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, id, "Ready")

      val request = FakeRequest("GET", s"/api/order/$id")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyReadyOrderHypermediaLinks(result, id)
    }
  }

  private def verifyUnpaidOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
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

  private def verifyPreparingOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(2)
    dapLinks must contain (DapLink("self", s"/api/order/$id", None))
    val mediaType = Some("application/vnd.restbucks+xml")
    dapLinks must contain (DapLink("/api/relations/latest", s"/api/order/$id", mediaType))
  }

  private def verifyReadyOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(3)
    dapLinks must contain (DapLink("self", s"/api/order/$id", None))
    val mediaType = Some("application/vnd.restbucks+xml")
    dapLinks must contain (DapLink("/api/relations/latest", s"/api/order/$id", mediaType))
    dapLinks must contain (DapLink("/api/relations/receive", s"/api/order/$id", mediaType))
  }
}
