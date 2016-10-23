package services

class OrderingService {

  import scala.xml.NodeSeq

  def newOrder(requestDoc: NodeSeq): (String, NodeSeq) = ???

  def getOrderStatus(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def updateOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def paymentReceived(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def getReceipt(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def receiveOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???

  def cancelOrder(id: String, requestDoc: NodeSeq): NodeSeq = ???
}
