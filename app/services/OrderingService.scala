package services

import models.{OrderRequest, OrderResponse}

class OrderingService(db: DatabaseService) {

  import scala.xml.NodeSeq

  def newOrder(requestDoc: NodeSeq): (String, NodeSeq) = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val orderResponse = OrderResponse(orderRequest.location, orderRequest.items, "payment-expected", 2.99)
    val id = "123"
    (id, orderResponse.toXML)
  }

  def getOrderStatus(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def updateOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def paymentReceived(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def getReceipt(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def receiveOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def cancelOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???
}
