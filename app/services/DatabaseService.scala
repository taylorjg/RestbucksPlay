package services

import models.{OrderResponse, Payment}

trait DatabaseService {
  def putOrder(orderResponse: OrderResponse): Int
  def getOrder(id: String): OrderResponse
  def deleteOrder(id: String): Unit
  def updateOrder(order: OrderResponse): Unit
  def putPayment(id: String, payment: Payment): Unit
  def getPayment(id: String): Payment
}
