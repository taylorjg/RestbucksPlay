package services

class OrderingService(db: DatabaseService) {

  import OrderStatuses._
  import models.{OrderRequest, OrderResponse, Payment, Receipt}
  import org.joda.time.DateTime

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import scala.xml.NodeSeq

  def newOrder(requestDoc: NodeSeq): (String, NodeSeq) = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val id = db.nextOrderId()
    val orderResponse = OrderResponse(orderRequest.location, orderRequest.items, id, PaymentExpected, 2.99)
    // TODO: Pass Seq(StateMachineManager) to each service method
    // TODO: paymentStateMachineManager.createResource(id.toString)
    (id.toString, orderResponse.toXML)
  }

  def getOrderStatus(id: String, requestDoc: NodeSeq): NodeSeq =
    db.getOrder(id).toXML

  def updateOrder(id: String, requestDoc: NodeSeq): NodeSeq = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(
      location = orderRequest.location,
      items = orderRequest.items
    )
    db.updateOrder(orderResponse2)
    orderResponse2.toXML
  }

  def paymentReceived(id: String, requestDoc: NodeSeq): NodeSeq = {
    val payment1 = Payment.fromXML(requestDoc.head)
    val payment2 = payment1.copy(paid = DateTime.now)
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(status = Preparing)
    db.updateOrder(orderResponse2)
    db.putPayment(id, payment2)
    prepareOrderAsync(id)
    payment2.toXML
  }

  def getReceipt(id: String, requestDoc: NodeSeq): NodeSeq = {
    val payment = db.getPayment(id)
    val receipt: Receipt = Receipt.fromPayment(payment)
    receipt.toXML
  }

  def orderPrepared(orderResponse: OrderResponse): Unit = {
    db.updateOrder(orderResponse.copy(status = Ready))
    // TODO: orderStateMachineManager.transitionTo(orderResponse.id.toString, "Ready");
  }

  def receiveOrder(id: String, requestDoc: NodeSeq): NodeSeq = {
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(status = Taken)
    db.updateOrder(orderResponse2)
    orderResponse2.toXML
  }

  def cancelOrder(id: String, requestDoc: NodeSeq): NodeSeq = {
    db.deleteOrder(id)
    NodeSeq.Empty
  }

  private def prepareOrderAsync(id: String): Future[Unit] = {
    Future {
      baristaWork(id)
    }
  }

  private def baristaWork(id: String) = {
    Thread.sleep(2000)
    val orderResponse = db.getOrder(id)
    orderPrepared(orderResponse)
  }
}
