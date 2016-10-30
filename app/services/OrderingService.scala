package services

class OrderingService(db: DatabaseService) {

  import OrderStatuses._
  import hypermedia.StateMachineManager
  import models.{OrderRequest, OrderResponse, Payment, Receipt}
  import org.joda.time.DateTime

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import scala.xml.NodeSeq

  def newOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq): (String, NodeSeq) = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val id = db.nextOrderId()
    val orderResponse = OrderResponse(orderRequest.location, orderRequest.items, id, PaymentExpected, 2.99)
    val paymentStateMachineManager = stateMachineManagers("payment")
    paymentStateMachineManager.createResource(id.toString)
    (id.toString, orderResponse.toXML)
  }

  def getOrderStatus(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq =
    db.getOrder(id).toXML

  def updateOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(
      location = orderRequest.location,
      items = orderRequest.items
    )
    db.updateOrder(orderResponse2)
    orderResponse2.toXML
  }

  def paymentReceived(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq = {
    val payment1 = Payment.fromXML(requestDoc.head)
    val payment2 = payment1.copy(paid = DateTime.now)
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(status = Preparing)
    db.updateOrder(orderResponse2)
    db.putPayment(id, payment2)
    val orderStateMachineManager = stateMachineManagers("order")
    prepareOrderAsync(id) // TODO: pass in orderStateMachineManager
    payment2.toXML
  }

  def getReceipt(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq = {
    val payment = db.getPayment(id)
    val receipt: Receipt = Receipt.fromPayment(payment)
    receipt.toXML
  }

  def orderPrepared(orderResponse: OrderResponse): Unit = {
    db.updateOrder(orderResponse.copy(status = Ready))
    // TODO: orderStateMachineManager.transitionTo(orderResponse.id.toString, "Ready");
  }

  def receiveOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq = {
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(status = Taken)
    db.updateOrder(orderResponse2)
    orderResponse2.toXML
  }

  def cancelOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): NodeSeq = {
    db.deleteOrder(id)
    NodeSeq.Empty
  }

  private def prepareOrderAsync(id: String): Future[Unit] = {
    Future {
      scala.concurrent.blocking {
        baristaWork(id)
      }
    }
  }

  private def baristaWork(id: String) = {
    Thread.sleep(2000)
    val orderResponse = db.getOrder(id)
    orderPrepared(orderResponse)
  }
}
