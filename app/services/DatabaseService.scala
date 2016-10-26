package services

import models.{OrderResponse, Payment}
import scala.xml.Node

trait DatabaseService {
  def newOrderFromXml(node: Node): Unit
  def getOrder(id: String): OrderResponse
  def updateOrder(order: OrderResponse): Unit
  def putPayment(id: String, payment: Payment): Unit
  def getPayment(id: String): Payment
  def deleteOrder(id: String): Unit
}
