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
    // TODO: manage order ids in this class rather than inside DatabaseService...
    // - need to persist next order id ?
    // - need to be able to control next order id from inside unit tests ?
    val orderResponse = OrderResponse(orderRequest.location, orderRequest.items, 0, PaymentExpected, 2.99)
    val id = db.putOrder(orderResponse)
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
    Thread.sleep(2 * 1000)
    val orderResponse1 = db.getOrder(id)
    val orderResponse2 = orderResponse1.copy(status = Ready)
    db.updateOrder(orderResponse2)
  }
}
