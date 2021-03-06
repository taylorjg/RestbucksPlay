package services

import hypermedia.State
import models.{OrderResponse, PaymentResponse}

trait DatabaseService {
  def nextOrderId(): Int
  def loadStatesMap(resource: String): Map[String, State]
  def saveStatesMap(resource: String, states: Map[String, State]): Unit
  def putOrder(orderResponse: OrderResponse): Unit
  def getOrder(id: String): Option[OrderResponse]
  def deleteOrder(id: String): Unit
  def updateOrder(order: OrderResponse): Unit
  def putPayment(id: String, paymentResponse: PaymentResponse): Unit
  def getPayment(id: String): Option[PaymentResponse]
}
