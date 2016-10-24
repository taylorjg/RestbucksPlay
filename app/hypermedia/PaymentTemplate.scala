package hypermedia

object PaymentTemplate {

  import TemplateConstants._
  import play.api.http.Status._

  private final val PaymentCreatedState = State("PaymentCreated", Some("PaymentExpected"), NoAccepts, NoLinks)

  private final val PaymentExpectedState = State("PaymentExpected", NoTransitionTo,
    Seq(
      Accept("PUT", "paymentReceived", CREATED, Some("PaymentReceived"), Seq(Error("NoValidPayment", BAD_REQUEST)))),
    Seq(
      Link("payment")))

  private final val PaymentReceivedState = State("PaymentReceived", NoTransitionTo,
    Seq(
      Accept("GET", "getReceipt", OK, NoTransitionTo, Seq(Error("NoSuchPayment", NOT_FOUND)))),
    Seq(
      Link("order", Some("/api/order/{id}")),
      Link("receipt")))

  final val template = StateMachineTemplate(
    "/api/payment/{id}",
    "application/vnd.restbucks+xml",
    "/api/relations",
    PaymentCreatedState,
    Map(PaymentExpectedState.name -> PaymentExpectedState),
    Seq(PaymentReceivedState))
}
