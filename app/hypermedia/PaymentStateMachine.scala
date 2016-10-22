package hypermedia

object PaymentStateMachine {

  private final val NoTransitionTo = None
  private final val NoAccepts: Seq[Accept] = Seq()
  private final val NoLinks: Seq[Link] = Seq()

  private final val PaymentCreatedState = State("PaymentCreated", Some("PaymentExpected"), NoAccepts, NoLinks)

  private final val PaymentExpectedState = State("PaymentExpected", NoTransitionTo,
    Seq(Accept("PUT", "PaymentReceived", 201, Some("PaymentReceived"), Seq(Error("NoValidPayment", 400)))),
    Seq(Link("payment")))

  private final val PaymentReceivedState = State("PaymentReceived", NoTransitionTo,
    Seq(Accept("GET", "GetReceipt", 200, NoTransitionTo, Seq(Error("NoSuchPayment", 404)))),
    Seq(
      Link("order", Some("/api/order/{id}")),
      Link("receipt")))

  final val stateMachineTemplate = StateMachineTemplate(
    "/payment/{id}",
    "Restbucks.OrderingService",
    "application/vnd.restbucks+xml",
    "http://relations.restbucks.com",
    PaymentCreatedState,
    Map(PaymentExpectedState.name -> PaymentExpectedState),
    Seq(PaymentReceivedState))
}
