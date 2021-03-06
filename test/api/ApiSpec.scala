package api

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Results

class ApiSpec extends PlaySpec
  with OneAppPerTest
  with Results {

  import hypermedia.{DapLink, OrderTemplate, PaymentTemplate}
  import models._
  import org.joda.time.DateTime
  import org.scalatest.TestData
  import play.api.Application
  import play.api.inject.bind
  import play.api.inject.guice.GuiceApplicationBuilder
  import play.api.mvc.Result
  import play.api.test.FakeRequest
  import play.api.test.Helpers._
  import services.{DatabaseService, OrderStatuses}

  import scala.concurrent.Future
  import scala.xml.XML

  private var mockDatabaseService: MockDatabaseService = _

  override def newAppForTest(testData: TestData): Application = {
    mockDatabaseService = new MockDatabaseService
    new GuiceApplicationBuilder()
      .overrides(bind[DatabaseService].toInstance(mockDatabaseService))
      .build
  }

  private final val SimpleOrderXml =
    <order>
      <location>takeAway</location>
      <item>
        <drink>latte</drink>
        <milk>skim</milk>
        <size>large</size>
      </item>
    </order>

  private final val InvalidOrderXml =
    <order>
      <!-- missing location element -->
      <item>
        <drink>latte</drink>
        <milk>skim</milk>
        <size>large</size>
      </item>
    </order>

  private final val PaymentXml =
    <payment>
      <amount>{4.5 * 1.5}</amount>
      <cardHolder>MR BRUCE FORSYTH</cardHolder>
      <cardNumber>4111111111111111</cardNumber>
      <expiryMonth>10</expiryMonth>
      <expiryYear>2018</expiryYear>
    </payment>

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

    val id = 42
    val request = FakeRequest("POST", "/api/order").withXmlBody(SimpleOrderXml).withHeaders(HostHeader)

    "return CREATED" in {
      mockDatabaseService.setNextOrderId(id)
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
    }

    "return a response body containing the correct content" in {
      mockDatabaseService.setNextOrderId(id)
      val Some(result) = route(app, request)
      val responseDoc = XML.loadString(contentAsString(result))
      val orderResponse = OrderResponse.fromXML(responseDoc.head)
      orderResponse.location must be("takeAway")
      orderResponse.items must be(Seq(OrderItem("latte", "skim", "large")))
      orderResponse.status must be("payment-expected")
      orderResponse.cost must be(4.5 * 1.5)
    }

    "return a response body containing the expected hypermedia links" in {
      mockDatabaseService.setNextOrderId(id)
      val Some(result) = route(app, request)
      verifyUnpaidOrderHypermediaLinks(result, id)
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
    "return a response body containing the correct content" in {
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
      orderResponse.cost must be(4.5 * 1.5)
    }
  }

  "putting a payment" should {

    val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
    val request = FakeRequest("PUT", s"/api/payment/${orderResponse.id}").withXmlBody(PaymentXml).withHeaders(HostHeader)

    "return OK" in {
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentExpected")
      val Some(result) = route(app, request)
      status(result) must be(CREATED)
    }

    "return a response body containing the correct content" in {
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentExpected")
      val Some(result) = route(app, request)
      val responseDoc = XML.loadString(contentAsString(result))
      val receivedPaymentResponse = PaymentResponse.fromXML(responseDoc.head)
      receivedPaymentResponse.amount must be(4.5 * 1.5)
      receivedPaymentResponse.cardHolder must be("MR BRUCE FORSYTH")
      receivedPaymentResponse.cardNumber must be("4111111111111111")
      receivedPaymentResponse.expiryMonth must be(10)
      receivedPaymentResponse.expiryYear must be(2018)
    }

    "return a response body containing the expected hypermedia links" in {
      mockDatabaseService.addOrderResponse(orderResponse)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentExpected")
      val Some(result) = route(app, request)
      verifyPaymentReceivedPaymentHypermediaLinks(result, orderResponse.id)
    }
  }

  "getting a receipt from a payment in the PaymentReceived state" should {

    val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
    val paymentResponse = PaymentResponse(orderResponse.cost, "MR BRUCE FORSYTH", "4111111111111111", 10, 2018, DateTime.now)
    val request = FakeRequest("GET", s"/api/payment/${orderResponse.id}").withHeaders(HostHeader)

    "return OK" in {
      mockDatabaseService.addPaymentResponse(orderResponse.id, paymentResponse)
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentReceived")
      val Some(result) = route(app, request)
      status(result) must be(OK)
    }

    "return a response body containing the correct content" in {
      mockDatabaseService.addPaymentResponse(orderResponse.id, paymentResponse)
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentReceived")
      val Some(result) = route(app, request)
      val responseDoc = XML.loadString(contentAsString(result))
      val receivedReceipt = Receipt.fromXML(responseDoc.head)
      receivedReceipt.amount must be(paymentResponse.amount)
      receivedReceipt.paid must be(paymentResponse.paid)
    }

    "return a response body containing the expected hypermedia links" in {
      mockDatabaseService.addPaymentResponse(orderResponse.id, paymentResponse)
      mockDatabaseService.setResourceState(PaymentTemplate.template, orderResponse.id, "PaymentReceived")
      val Some(result) = route(app, request)
      verifyPaymentReceivedPaymentHypermediaLinks(result, orderResponse.id)
    }
  }

  "creating a new order with invalid XML" should {
    "return BAD_REQUEST" in {
      val request = FakeRequest("POST", "/api/order").withXmlBody(InvalidOrderXml).withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include("location")
    }
  }

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

  "an exception being thrown" should {
    "return INTERNAL_SERVER_ERROR" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      mockDatabaseService.setThrowDeliberateExceptionFlag()
      val request = FakeRequest("GET", s"/api/order/${orderResponse.id}").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }
  }

  "a request using a verb that is not expected in the current state" should {
    "return METHOD_NOT_ALLOWED" in {
      val orderResponse = simpleOrderResponse(OrderStatuses.PaymentExpected)
      mockDatabaseService.setResourceState(OrderTemplate.template, orderResponse.id, "Unpaid")
      val request = FakeRequest("PUT", s"/api/order/${orderResponse.id}").withXmlBody(SimpleOrderXml).withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(METHOD_NOT_ALLOWED)
    }
  }

  "requesting an unknown resource" should {
    "return NOT_FOUND" in {
      val request = FakeRequest("GET", "/api/bogus/1").withHeaders(HostHeader)
      val Some(result) = route(app, request)
      status(result) must be(NOT_FOUND)
    }
  }

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

  private def verifyPaymentReceivedPaymentHypermediaLinks(result: Future[Result], id: Int): Unit = {
    val responseDoc = XML.loadString(contentAsString(result))
    val dapLinks = responseDoc \ "link" map DapLink.fromXML
    dapLinks.length must be(3)
    dapLinks must contain(DapLink("self", absUri(s"/api/payment/$id"), None))
    dapLinks must contain(DapLink("/api/relations/order", absUri(s"/api/order/$id"), MediaType))
    dapLinks must contain(DapLink("/api/relations/receipt", absUri(s"/api/payment/$id"), MediaType))
  }

  private def absUri(path: String): String = s"http://$Host$path"
}
