package services

import play.api.mvc.Result
import play.api.mvc.Results._


class OrderingService(db: DatabaseService) {

  import OrderStatuses._
  import hypermedia.StateMachineManager
  import models._
  import org.joda.time.DateTime

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  import scala.xml.NodeSeq

  def newOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq): Either[Result, (String, NodeSeq)] = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val id = db.nextOrderId()
    val cost = CostCalculatorService.calculateCost(orderRequest)
    val orderResponse = OrderResponse(orderRequest.location, orderRequest.items, id, PaymentExpected, cost)
    val paymentStateMachineManager = stateMachineManagers("payment")
    paymentStateMachineManager.createResource(id.toString)
    Right((id.toString, orderResponse.toXML))
  }

  def getOrderStatus(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] =
    db.getOrder(id) match {
      case Some(orderResponse) => Right(orderResponse.toXML)
      case None => Left(NotFound)
    }

  def updateOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] = {
    val orderRequest = OrderRequest.fromXML(requestDoc.head)
    val orderResponse1 = db.getOrder(id).get
    val orderResponse2 = orderResponse1.copy(
      location = orderRequest.location,
      items = orderRequest.items
    )
    db.updateOrder(orderResponse2)
    Right(orderResponse2.toXML)
  }

  def paymentReceived(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] = {
    val paymentRequest = PaymentRequest.fromXML(requestDoc.head)
    val paymentResponse = PaymentResponse(
      paymentRequest.amount,
      paymentRequest.cardHolder,
      paymentRequest.cardNumber,
      paymentRequest.expiryMonth,
      paymentRequest.expiryYear,
      DateTime.now)
    val orderResponse1 = db.getOrder(id).get
    val orderResponse2 = orderResponse1.copy(status = Preparing)
    db.updateOrder(orderResponse2)
    db.putPayment(id, paymentResponse)
    val orderStateMachineManager = stateMachineManagers("order")
    prepareOrderAsync(orderStateMachineManager, id)
    Right(paymentResponse.toXML)
  }

  def getReceipt(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] = {
    val paymentResponse = db.getPayment(id).get
    val receipt = Receipt.fromPaymentResponse(paymentResponse)
    Right(receipt.toXML)
  }

  def orderPrepared(orderStateMachineManager: StateMachineManager, orderResponse: OrderResponse): Unit = {
    db.updateOrder(orderResponse.copy(status = Ready))
    orderStateMachineManager.transitionTo(orderResponse.id.toString, "Ready")
  }

  def receiveOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] = {
    val orderResponse1 = db.getOrder(id).get
    val orderResponse2 = orderResponse1.copy(status = Taken)
    db.updateOrder(orderResponse2)
    Right(orderResponse2.toXML)
  }

  def cancelOrder(stateMachineManagers: Map[String, StateMachineManager], requestDoc: NodeSeq, id: String): Either[Result, NodeSeq] = {
    db.deleteOrder(id)
    Right(NodeSeq.Empty)
  }

  private def prepareOrderAsync(orderStateMachineManager: StateMachineManager, id: String): Future[Unit] = {
    Future {
      scala.concurrent.blocking {
        baristaWork(orderStateMachineManager, id)
      }
    }
  }

  private def baristaWork(orderStateMachineManager: StateMachineManager, id: String) = {
    Thread.sleep(2000)
    val orderResponse = db.getOrder(id).get
    orderPrepared(orderStateMachineManager, orderResponse)
  }
}
