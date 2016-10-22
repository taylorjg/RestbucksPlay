package hypermedia

object PaymentTemplate {

  import TemplateConstants._
  import play.api.http.Status._

  private final val PaymentCreatedState = State("PaymentCreated", Some("PaymentExpected"), NoAccepts, NoLinks)

  private final val PaymentExpectedState = State("PaymentExpected", NoTransitionTo,
    Seq(
      Accept("PUT", "PaymentReceived", CREATED, Some("PaymentReceived"), Seq(Error("NoValidPayment", BAD_REQUEST)))),
    Seq(
      Link("payment")))

  private final val PaymentReceivedState = State("PaymentReceived", NoTransitionTo,
    Seq(
      Accept("GET", "GetReceipt", OK, NoTransitionTo, Seq(Error("NoSuchPayment", NOT_FOUND)))),
    Seq(
      Link("order", Some("/api/order/{id}")),
      Link("receipt")))

  final val template = StateMachineTemplate(
    "/api/payment/{id}",
    "Restbucks.OrderingService",
    "application/vnd.restbucks+xml",
    "http://relations.restbucks.com",
    PaymentCreatedState,
    Map(PaymentExpectedState.name -> PaymentExpectedState),
    Seq(PaymentReceivedState))
}
