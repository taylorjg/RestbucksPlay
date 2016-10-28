package api

import hypermedia.{State, StateMachineTemplate}
import models.{OrderResponse, Payment}
import services.DatabaseService

class MockDatabaseService extends DatabaseService {

  private var orders = Map[Int, OrderResponse]()
  private var payments = Map[Int, Payment]()
  private var resourcesToStatesMaps = Map[String, Map[String, State]]()
  private var nextOrderId = 0

  def addOrderResponse(orderResponse: OrderResponse): Unit = orders = orders + (orderResponse.id -> orderResponse)
  def setNextOrderId(id: Int): Unit = nextOrderId = id
  def setResourceState(template: StateMachineTemplate, id: Int, stateName: String): Unit = {
    val k = template.uriTemplate
    val v = Map(id.toString -> template.states(stateName))
    resourcesToStatesMaps = resourcesToStatesMaps + (k -> v)
  }

  override def loadStatesMap(resource: String): Map[String, State] =
    resourcesToStatesMaps.getOrElse(resource, Map())

  override def saveStatesMap(resource: String, states: Map[String, State]): Unit =
    resourcesToStatesMaps = resourcesToStatesMaps.updated(resource, states)

  override def putOrder(orderResponse: OrderResponse): Int = {
    orders = orders + (nextOrderId -> orderResponse)
    nextOrderId
  }

  override def getOrder(id: String): OrderResponse =
    orders(id.toInt)

  override def deleteOrder(id: String): Unit =
    orders = orders - id.toInt

  override def updateOrder(orderResponse: OrderResponse): Unit =
    orders = orders.updated(orderResponse.id, orderResponse)

  override def putPayment(id: String, payment: Payment): Unit =
    payments = payments + (id.toInt -> payment)

  override def getPayment(id: String): Payment =
    payments(id.toInt)
}
