package api

import hypermedia.{State, StateMachineTemplate}
import models.{OrderResponse, PaymentResponse}
import services.DatabaseService

class MockDatabaseService extends DatabaseService {

  private var orders = Map[Int, OrderResponse]()
  private var payments = Map[Int, PaymentResponse]()
  private var resourcesToStatesMaps = Map[String, Map[String, State]]()
  private var orderId = 0

  def addOrderResponse(orderResponse: OrderResponse): Unit = orders = orders + (orderResponse.id -> orderResponse)
  def addPaymentResponse(id: Int, paymentResponse: PaymentResponse): Unit = payments = payments + (id -> paymentResponse)
  def setNextOrderId(id: Int): Unit = orderId = id
  def setResourceState(template: StateMachineTemplate, id: Int, stateName: String): Unit = {
    val k = template.uriTemplate
    val v = Map(id.toString -> template.states(stateName))
    resourcesToStatesMaps = resourcesToStatesMaps + (k -> v)
  }

  override def nextOrderId(): Int =
    orderId

  override def loadStatesMap(resource: String): Map[String, State] =
    resourcesToStatesMaps.getOrElse(resource, Map())

  override def saveStatesMap(resource: String, states: Map[String, State]): Unit =
    resourcesToStatesMaps = resourcesToStatesMaps.updated(resource, states)

  override def putOrder(orderResponse: OrderResponse): Unit =
    orders = orders + (nextOrderId -> orderResponse)

  override def getOrder(id: String): Option[OrderResponse] =
    orders.get(id.toInt)

  override def deleteOrder(id: String): Unit =
    orders = orders - id.toInt

  override def updateOrder(orderResponse: OrderResponse): Unit =
    orders = orders.updated(orderResponse.id, orderResponse)

  override def putPayment(id: String, paymentResponse: PaymentResponse): Unit =
    payments = payments + (id.toInt -> paymentResponse)

  override def getPayment(id: String): Option[PaymentResponse] =
    payments.get(id.toInt)
}
