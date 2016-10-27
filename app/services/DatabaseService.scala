package services

import hypermedia.State
import models.{OrderResponse, Payment}

trait DatabaseService {
  def loadStatesMap(resource: String): Map[String, State]
  def saveStatesMap(resource: String, states: Map[String, State]): Unit
  def putOrder(orderResponse: OrderResponse): Int
  def getOrder(id: String): OrderResponse
  def deleteOrder(id: String): Unit
  def updateOrder(order: OrderResponse): Unit
  def putPayment(id: String, payment: Payment): Unit
  def getPayment(id: String): Payment
}
