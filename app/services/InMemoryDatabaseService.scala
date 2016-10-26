package services
import models.{OrderResponse, Payment}

import scala.xml.Node

class InMemoryDatabaseService extends DatabaseService {

  override def newOrderFromXml(node: Node): Unit = ???

  override def getOrder(id: String): OrderResponse = ???

  override def updateOrder(order: OrderResponse): Unit = ???

  override def putPayment(id: String, payment: Payment): Unit = ???

  override def getPayment(id: String): Payment = ???

  override def deleteOrder(id: String): Unit = ???
}
