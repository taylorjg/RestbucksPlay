package services
import models.{Order, Payment}

import scala.xml.Node

class InMemoryDatabaseService extends DatabaseService {

  override def newOrderFromXml(node: Node): Unit = ???

  override def getOrder(id: String): Order = ???

  override def updateOrder(order: Order): Unit = ???

  override def putPayment(id: String, payment: Payment): Unit = ???

  override def getPayment(id: String): Payment = ???

  override def deleteOrder(id: String): Unit = ???
}
