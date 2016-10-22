package services

class OrderingService {

  import scala.xml.NodeSeq

  def newOrder(requestDoc: NodeSeq, responseDoc: NodeSeq): String = ???
  def getOrderStatus(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
  def updateOrder(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
  def paymentReceived(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
  def getReceipt(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
  def receiveOrder(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
  def cancelOrder(id: String, requestDoc: NodeSeq, responseDoc: NodeSeq): Unit = ???
}
