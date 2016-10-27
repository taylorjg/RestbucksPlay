package services

import models.{OrderRequest, OrderResponse}

class OrderingService(db: DatabaseService) {

  import scala.xml.NodeSeq

  def newOrder(requestDoc: NodeSeq): (String, NodeSeq) = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val orderResponse1 = OrderResponse(orderRequest.location, orderRequest.items, 0, "payment-expected", 2.99)
    val id = db.putOrder(orderResponse1)
    val orderResponse2 = orderResponse1.copy(id = id) // TODO: Yuk! make this better - it makes me sad!
    (id.toString, orderResponse2.toXML)
  }

  def getOrderStatus(id: String, requestDoc: NodeSeq): NodeSeq = db.getOrder(id).toXML

  def updateOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def paymentReceived(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def getReceipt(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def receiveOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def cancelOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???
}
