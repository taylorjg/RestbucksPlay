package hypermedia

import models.DapLink
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}

class StateMachineManager(private val template: StateMachineTemplate) {

  import scala.xml.{Node, NodeSeq, Elem}

  val uriTemplate: String = template.uriTemplate

  private var states: Map[String, State] = Map()

  def process(request: Request[NodeSeq]): Result = {

    val pos = uriTemplate indexOf "/{"
    val baseUri = uriTemplate take pos

    val initialStateAccept = template.initialState.accepts find { a => a.httpVerb == request.method }
    if (initialStateAccept.isDefined && request.uri.reverse.dropWhile(c => c == '/').reverse == baseUri) {
      val method = getMethod1(initialStateAccept.get)
      val (id, responseDoc) = method(request.body)
      val newStateName = initialStateAccept.get.transitionTo.getOrElse(template.initialState.name)
      // TODO: return correct status code: accept.response or an error.response value or 500
      Ok(transition(id, newStateName, responseDoc))
    }
    else {
      val id = "" // TODO: extract the id from the end of request.uri
      val currentState = states(id)
      val acceptOption = currentState.accepts find { a => a.httpVerb == request.method}
      acceptOption match {
        case Some(accept) =>
          val method = getMethod2(accept)
          val responseDoc = method(id, request.body)
          val newStateName = accept.transitionTo.getOrElse(currentState.name)
          // TODO: return correct status code: accept.response or an error.response value or 500
          Ok(transition(id, newStateName, responseDoc))
        case None =>
      }
    }

    val responseDoc =
      <response>
        <method>
          {request.method}
        </method>
        <uri>
          {request.uri}
        </uri>
        <headers>
          {request.headers.headers map {
          case (n, v) =>
            <header>
              <name>
                {n}
              </name>
              <value>
                {v}
              </value>
            </header>
        }}
        </headers>
      </response>

    Ok(responseDoc)
  }

  def transitionTo(id: String, stateName: String): Unit =
    states += id -> template.states(stateName)

  // Return method with name accept.method in class template.className
  private def getMethod1(accept: Accept): NodeSeq => (String, NodeSeq) =
    ???

  // Return method with name accept.method in class template.className
  private def getMethod2(accept: Accept): (String, NodeSeq) => NodeSeq =
    ???

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
