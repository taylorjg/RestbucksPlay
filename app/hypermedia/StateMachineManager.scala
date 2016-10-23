package hypermedia

import models.DapLink
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}

class StateMachineManager(private val template: StateMachineTemplate) {

  import scala.xml.{Node, NodeSeq, Elem}
  import scala.reflect.runtime.universe._

  val uriTemplate: String = template.uriTemplate

  private var states: Map[String, State] = Map()

  def process(request: Request[NodeSeq]): Result = {

    val pos = uriTemplate indexOf "/{"
    val baseUri = uriTemplate take pos

    val initialStateAccept = template.initialState.accepts find { a => a.httpVerb == request.method }
    if (initialStateAccept.isDefined && request.uri.reverse.dropWhile(c => c == '/').reverse == baseUri) {
      val methodMirror = getMethod(initialStateAccept.get)
      val result = methodMirror(request.body)
      result match {
        case (id: String, responseDoc: NodeSeq) =>
          val newStateName = initialStateAccept.get.transitionTo.getOrElse(template.initialState.name)
          // TODO: handle errors (accept.errors)
          val statusCode = initialStateAccept.get.response
          new Status(statusCode).apply(transition(id, newStateName, responseDoc))
        case other => throw new Exception(s"methodMirror returned $other")
      }
    }
    else {
      val id = request.uri.drop(pos + 1)
      // TODO: use states.get(id) instead to give Option[State]
      val currentState = states(id)
      val acceptOption = currentState.accepts find { a => a.httpVerb == request.method}
      acceptOption match {
        case Some(accept) =>
          val methodMirror = getMethod(accept)
          val result = methodMirror(id, request.body)
          result match {
            case responseDoc: NodeSeq =>
              val newStateName = accept.transitionTo.getOrElse(currentState.name)
              // TODO: handle errors (accept.errors)
              val statusCode = accept.response
              new Status(statusCode).apply(transition(id, newStateName, responseDoc))
            case other => throw new Exception(s"methodMirror returned $other")
          }
        case None => throw new Exception(s"No matching Accept found in current state for id $id")
      }
    }
  }

  def transitionTo(id: String, stateName: String): Unit =
    states += id -> template.states(stateName)

  private def getMethod(accept: Accept): MethodMirror = {
      val service = new services.OrderingService
      val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(service)
      val methodSymbol = instanceMirror.symbol.info.member(TermName(accept.method)).asMethod
      instanceMirror.reflectMethod(methodSymbol)
    }

  private def transition(id: String, stateName: String, responseDoc: NodeSeq): NodeSeq = {
    val state = template.states(stateName)
    states += id -> state
    val selfDapLink = DapLink("self", template.uriTemplate.replace("{id}", id), None)
    // TODO: add the other DAP links from state.links (fold over a Seq of DapLink ?)
    addChild(responseDoc, selfDapLink.toXML)
  }

  private def addChild(doc: NodeSeq, child: Node): NodeSeq =
    doc.headOption match {
      case Some(e: Elem) => e.copy(child = e.child ++ child)
      case other => doc
    }
}
