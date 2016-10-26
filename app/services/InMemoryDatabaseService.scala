package services

class InMemoryDatabaseService extends DatabaseService {

  import models.{OrderResponse, Payment}

  private var orders = Map[Int, OrderResponse]()
  private var payments = Map[Int, Payment]()
  private var currentOrderId = 0

  override def putOrder(orderResponse: OrderResponse): Int = {
    currentOrderId += 1
    orders = orders + (currentOrderId -> orderResponse)
    currentOrderId
  }

  override def getOrder(id: String): OrderResponse = ???

  override def updateOrder(order: OrderResponse): Unit = ???

  override def deleteOrder(id: String): Unit = ???

  override def putPayment(id: String, payment: Payment): Unit = ???

  override def getPayment(id: String): Payment = ???
}
