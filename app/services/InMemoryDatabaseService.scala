package services
import hypermedia.State

class InMemoryDatabaseService extends DatabaseService {

  import models.{OrderResponse, PaymentResponse}

  private var orders = Map[String, OrderResponse]()
  private var payments = Map[String, PaymentResponse]()
  private var resourcesToStatesMaps = Map[String, Map[String, State]]()
  private var currentOrderId = 0

  override def nextOrderId(): Int = {
    currentOrderId += 1
    currentOrderId
  }

  override def loadStatesMap(resource: String): Map[String, State] =
    resourcesToStatesMaps.getOrElse(resource, Map())

  override def saveStatesMap(resource: String, states: Map[String, State]): Unit =
    resourcesToStatesMaps = resourcesToStatesMaps.updated(resource, states)

  override def putOrder(orderResponse: OrderResponse): Unit =
    orders = orders + (currentOrderId.toString -> orderResponse)

  override def getOrder(id: String): OrderResponse =
    orders(id)

  override def updateOrder(orderResponse: OrderResponse): Unit =
    orders = orders.updated(orderResponse.id.toString, orderResponse)

  override def deleteOrder(id: String): Unit =
    orders = orders - id

  override def putPayment(id: String, paymentResponse: PaymentResponse): Unit =
    payments = payments + (id -> paymentResponse)

  override def getPayment(id: String): PaymentResponse =
    payments(id)
}
