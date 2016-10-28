package api

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

import scala.concurrent.Future

class ApiSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import hypermedia.{DapLink, OrderTemplate}
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

  private var mockDatabaseService: MockDatabaseService = _

  override def newAppForTest(testData: TestData): Application = {
    mockDatabaseService = new MockDatabaseService
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

  private def simpleOrderResponse(status: String): OrderResponse =
    OrderResponse(
      "takeAway",
      Seq(OrderItem("milk", "latte", "large")),
      42,
      status,
      2.99)

  private final val MediaType = Some("application/vnd.restbucks+xml")

  "creating a new order" should {
    "return a response body containing the expected hypermedia links" in {
      mockDatabaseService.setNextOrderId(42)
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
      verifyUnpaidOrderHypermediaLinks(result, 42)
    }
  }

  "getting an order in the Unpaid state" should {
    "return a response body containing the expected hypermedia links" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyUnpaidOrderHypermediaLinks(result, orderResponse.id)
    }
  }

  "getting an order in the Preparing state" should {
    "return a response body containing the expected hypermedia links" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.Preparing)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Preparing")
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyPreparingOrderHypermediaLinks(result, orderResponse.id)
    }
  }

  "getting an order in the Ready state" should {
    "return a response body containing the expected hypermedia links" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.Ready)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Ready")
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}")
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyReadyOrderHypermediaLinks(result, orderResponse.id)
    }
  }

  private def verifyUnpaidOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(5)
    dapLinks must contain(DapLink("self", s"/api/order/$id", None))
    dapLinks must contain(DapLink("/api/relations/latest", s"/api/order/$id", MediaType))
    dapLinks must contain(DapLink("/api/relations/update", s"/api/order/$id", MediaType))
    dapLinks must contain(DapLink("/api/relations/payment", s"/api/payment/$id", MediaType))
    dapLinks must contain(DapLink("/api/relations/cancel", s"/api/order/$id", MediaType))
  }

  private def verifyPreparingOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(2)
    dapLinks must contain(DapLink("self", s"/api/order/$id", None))
    dapLinks must contain(DapLink("/api/relations/latest", s"/api/order/$id", MediaType))
  }

  private def verifyReadyOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(3)
    dapLinks must contain(DapLink("self", s"/api/order/$id", None))
    dapLinks must contain(DapLink("/api/relations/latest", s"/api/order/$id", MediaType))
    dapLinks must contain(DapLink("/api/relations/receive", s"/api/order/$id", MediaType))
  }
}
