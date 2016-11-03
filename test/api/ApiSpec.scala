package api

import hypermedia.PaymentTemplate
import org.joda.time.DateTime
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

import scala.concurrent.Future

class ApiSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import hypermedia.{DapLink, OrderTemplate}
  import models._
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
      Seq(OrderItem("latte", "skim", "large")),
      42,
      status,
      4.5 * 1.5)

  private final val MediaType = Some("application/vnd.restbucks+xml")
  private final val Host = "localhost:9000"
  private final val HostHeader = "host" -> Host

  "creating a new order" should {

    "return a response body containing the expected hypermedia links" in {
      mockDatabaseService.setNextOrderId(42)
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder).withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
      verifyUnpaidOrderHypermediaLinks(result, 42)
    }

    "return a response body containing the correct location, items, status and cost" in {
      mockDatabaseService.setNextOrderId(42)
      val request = FakeRequest("POST", "/api/order").withXmlBody(simpleOrder).withHeaders(HostHeader)
      val Some(result) = route(app, request)
      val responseDoc = XML.loadString(contentAsString(result))
      val orderResponse = OrderResponse.fromXML(responseDoc.head)
      orderResponse.location must be("takeAway")
      orderResponse.items must be(Seq(OrderItem("latte", "skim", "large")))
      orderResponse.status must be("payment-expected")
      orderResponse.cost must be(4.5 * 1.5)
    }
  }

  "getting an order in the Unpaid state" should {
    "return a response body containing the expected hypermedia links" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
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
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
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
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(OK)
      verifyReadyOrderHypermediaLinks(result, orderResponse.id)
    }
  }

  "deleting an order in the Unpaid state" should {
    "return a response body with no content" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      val request = FakeRequest("DELETE", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(OK)
      val responseContentLength = contentAsBytes(result)
      responseContentLength.length must be(0)
    }
  }

  "deleting an order in the Ready state" should {
    "return a response body containing the correct location, items and status" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.Ready)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Ready")
      val request = FakeRequest("DELETE", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(OK)
      val responseDoc = XML.loadString(contentAsString(result))
      val receivedOrderResponse = OrderResponse.fromXML(responseDoc.head)
      receivedOrderResponse.location must be("takeAway")
      receivedOrderResponse.items must be(Seq(OrderItem("latte", "skim", "large")))
      receivedOrderResponse.status must be("taken")
    }
  }

  "putting a payment" should {
    "return OK" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentExpected")
      val payment =
        <payment>
          <amount>2.99</amount>
          <cardHolder>MR BRUCE FORSYTH</cardHolder>
          <cardNumber>4111111111111111</cardNumber>
          <expiryMonth>10</expiryMonth>
          <expiryYear>2018</expiryYear>
        </payment>
      val request = FakeRequest("PUT", s"/api/payment/${orderResponse.id}").withXmlBody(payment).withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
      // TODO: verify the content and links in the response body
    }
  }

  "getting a payment in the PaymentReceived state" should {
    "return OK" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      val paymentResponse = PaymentResponse(2.99, "MR BRUCE FORSYTH", "4111111111111111", 10, 2018, DateTime.now)
      mockDatabaseService.addPaymentResponse(orderResponse.id, paymentResponse)
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentReceived")
      val request = FakeRequest("GET", s"/api/payment/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(OK)
    }
  }

//  "creating a new order with missing request XML elements" should {
//    "return BAD_REQUEST" in {
//      val xml =
//        <order xmlns="http://schemas.restbucks.com">
//          <!-- missing location element -->
//          <item>
//            <drink>latte</drink>
//            <milk>skim</milk>
//            <size>large</size>
//          </item>
//        </order>
//      val request = FakeRequest("POST", "/api/order").withXmlBody(xml).withHeaders(HostHeader)
//      val Some(result) = route(app, request)
//      status(result) must be(BAD_REQUEST)
//    }
//  }

  "getting an order that does not exist in the states map" should {
    "return NOT_FOUND" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(NOT_FOUND)
    }
  }

  "getting an order that exists in the states map but does not exist in the database" should {
    "return NOT_FOUND" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(NOT_FOUND)
    }
  }

  // Add a test re INTERNAL_SERVER_ERROR
  // - add a method to MockDatabaseService to force an exception to be thrown

  private def verifyUnpaidOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(5)
    dapLinks must contain(DapLink("self", absUri(s"/api/order/$id"), None))
    dapLinks must contain(DapLink("/api/relations/latest", absUri(s"/api/order/$id"), MediaType))
    dapLinks must contain(DapLink("/api/relations/update", absUri(s"/api/order/$id"), MediaType))
    dapLinks must contain(DapLink("/api/relations/payment", absUri(s"/api/payment/$id"), MediaType))
    dapLinks must contain(DapLink("/api/relations/cancel", absUri(s"/api/order/$id"), MediaType))
  }

  private def verifyPreparingOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(2)
    dapLinks must contain(DapLink("self", absUri(s"/api/order/$id"), None))
    dapLinks must contain(DapLink("/api/relations/latest", absUri(s"/api/order/$id"), MediaType))
  }

  private def verifyReadyOrderHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(3)
    dapLinks must contain(DapLink("self", absUri(s"/api/order/$id"), None))
    dapLinks must contain(DapLink("/api/relations/latest", absUri(s"/api/order/$id"), MediaType))
    dapLinks must contain(DapLink("/api/relations/receive", absUri(s"/api/order/$id"), MediaType))
  }

  private def absUri(path: String): String = s"http://$Host$path"
}
