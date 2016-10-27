package services
import hypermedia.State

class InMemoryDatabaseService extends DatabaseService {

  import models.{OrderResponse, Payment}

  private var orders = Map[String, OrderResponse]()
  private var payments = Map[String, Payment]()
  private var resourcesToStatesMaps = Map[String, Map[String, State]]()
  private var currentOrderId = 0

  override def loadStatesMap(resource: String): Map[String, State] =
    resourcesToStatesMaps(resource)

  override def saveStatesMap(resource: String, states: Map[String, State]): Unit =
    resourcesToStatesMaps = resourcesToStatesMaps.updated(resource, states)

  override def putOrder(orderResponse: OrderResponse): Int = {
    currentOrderId += 1
    orders = orders + (currentOrderId.toString -> orderResponse)
    currentOrderId
  }

  override def getOrder(id: String): OrderResponse =
    orders(id)

  override def updateOrder(orderResponse: OrderResponse): Unit =
    orders = orders.updated(orderResponse.id.toString, orderResponse)

  override def deleteOrder(id: String): Unit =
    orders = orders - id

  override def putPayment(id: String, payment: Payment): Unit =
    payments = payments + (id -> payment)

  override def getPayment(id: String): Payment =
    payments(id)
}
