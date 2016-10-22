package hypermedia

object OrderStateMachine {

  private final val NoTransitionTo = None
  private final val NoAccepts: Seq[Accept] = Seq()
  private final val NoLinks: Seq[Link] = Seq()
  private final val NoErrors: Seq[Error] = Seq()

  private final val OrderCreatedState = State("OrderCreated", NoTransitionTo,
    Seq(Accept("POST", "NewOrder", 201, Some("Unpaid"), Seq(Error("NotValidOrder", 400)))),
    NoLinks)

  private final val UnpaidState = State("Unpaid", NoTransitionTo,
    Seq(
      Accept("GET", "GetOrderStatus", 200, NoTransitionTo, Seq(Error("NoSuchOrder", 404))),
      Accept("POST", "UpdateOrder", 200, NoTransitionTo, NoErrors),
      Accept("DELETE", "CancelOrder", 200, Some("Cancelled"), NoErrors)
    ),
    Seq(
      Link("latest"),
      Link("update"),
      Link("payment", Some("/payment/{id}")),
      Link("cancel")))

  private final val PreparingState = State("Preparing", NoTransitionTo,
    Seq(Accept("GET", "GetOrderStatus", 200, NoTransitionTo, NoErrors)),
    Seq(Link("latest")))

  private final val ReadyState = State("Ready", NoTransitionTo,
    Seq(
      Accept("GET", "GetOrderStatus", 200, NoTransitionTo, NoErrors),
      Accept("DELETE", "ReceiveOrder", 200, Some("Delivered"), NoErrors)),
    Seq(
      Link("latest"),
      Link("receive")))

  private final val DeliveredState = State("Delivered", NoTransitionTo, NoAccepts, NoLinks)
  private final val CancelledState = State("Cancelled", NoTransitionTo, NoAccepts, NoLinks)

  final val stateMachineTemplate = StateMachineTemplate(
    "/api/order/{id}",
    "Restbucks.OrderingService",
    "application/vnd.restbucks+xml",
    "http://relations.restbucks.com",
    OrderCreatedState,
    Map(
      UnpaidState.name -> UnpaidState,
      PreparingState.name -> PreparingState,
      ReadyState.name -> ReadyState),
    Seq(DeliveredState, CancelledState))
}
