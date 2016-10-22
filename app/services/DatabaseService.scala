package services

import models.{Order, Payment}

trait DatabaseService {
  def newOrderFromXml(node: scala.xml.Node): Unit
  def getOrder(id: String): Order
  def updateOrder(order: Order): Unit
  def putPayment(id: String, payment: Payment): Unit
  def getPayment(id: String): Payment
  def deleteOrder(id: String): Unit
}
