package hypermedia

object OrderTemplate {

  import TemplateConstants._
  import play.api.http.Status._

  private final val OrderCreatedState = State("OrderCreated", NoTransitionTo,
    Seq(
      Accept("POST", "newOrder", CREATED, Some("Unpaid"), Seq(Error("NotValidOrder", BAD_REQUEST)))),
    NoLinks)

  private final val UnpaidState = State("Unpaid", NoTransitionTo,
    Seq(
      Accept("GET", "getOrderStatus", OK, NoTransitionTo, Seq(Error("NoSuchOrder", NOT_FOUND))),
      Accept("POST", "updateOrder", OK, NoTransitionTo, NoErrors),
      Accept("DELETE", "cancelOrder", OK, Some("Cancelled"), NoErrors)
    ),
    Seq(
      Link("latest"),
      Link("update"),
      Link("payment", Some("/api/payment/{id}")),
      Link("cancel")))

  private final val PreparingState = State("Preparing", NoTransitionTo,
    Seq(Accept("GET", "getOrderStatus", OK, NoTransitionTo, NoErrors)),
    Seq(Link("latest")))

  private final val ReadyState = State("Ready", NoTransitionTo,
    Seq(
      Accept("GET", "getOrderStatus", OK, NoTransitionTo, NoErrors),
      Accept("DELETE", "receiveOrder", OK, Some("Delivered"), NoErrors)),
    Seq(
      Link("latest"),
      Link("receive")))

  private final val DeliveredState = State("Delivered", NoTransitionTo, NoAccepts, NoLinks)
  private final val CancelledState = State("Cancelled", NoTransitionTo, NoAccepts, NoLinks)

  final val template = StateMachineTemplate(
    "/api/order/{id}",
    "services.OrderingService",
    "application/vnd.restbucks+xml",
    "/api/relations",
    OrderCreatedState,
    Map(
      UnpaidState.name -> UnpaidState,
      PreparingState.name -> PreparingState,
      ReadyState.name -> ReadyState),
    Seq(DeliveredState, CancelledState))
}
