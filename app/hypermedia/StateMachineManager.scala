package hypermedia

class StateMachineManager(private val template: StateMachineTemplate, private val service: Any) {

  import play.api.mvc.Results._
  import play.api.mvc.{Request, Result}

  import scala.xml.{Elem, Node, NodeSeq}
  import scala.reflect.runtime.universe._

  val uriTemplate: String = template.uriTemplate

  private var states: Map[String, State] = Map()
  private val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(service)

  def process(request: Request[NodeSeq]): Result = {

    val pos = uriTemplate indexOf "/{"
    val baseUri = uriTemplate take pos
    val trimmedRequestUri = request.uri.trim('/')

    template.initialState.accepts find (a => a.httpVerb == request.method) match {
      case Some(accept) if trimmedRequestUri == baseUri =>
        val methodMirror = getMethod(accept)
        methodMirror(request.body) match {
          case (id: String, responseDoc: NodeSeq) =>
            val newStateName = accept.transitionTo.getOrElse(template.initialState.name)
            // TODO: handle accept.errors
            // TODO: handle internal server errors
            val statusCode = accept.response
            new Status(statusCode).apply(transition(id, newStateName, responseDoc))
          case other =>
            throw new Exception(s"accept method returned unexpected value, $other")
        }
      case _ =>
        val id = request.uri.drop(pos + 1)
        (for {
          currentState <- states.get(id)
          accept <- currentState.accepts find (a => a.httpVerb == request.method)
        } yield {
          val methodMirror = getMethod(accept)
          methodMirror(id, request.body) match {
            case responseDoc: NodeSeq =>
              val newStateName = accept.transitionTo.getOrElse(currentState.name)
              // TODO: handle accept.errors
              // TODO: handle internal server errors
              val statusCode = accept.response
              new Status(statusCode).apply(transition(id, newStateName, responseDoc))
            case other =>
              throw new Exception(s"accept method returned unexpected value, $other")
          }
        }) getOrElse InternalServerError("TODO: add error message...")
    }
  }

  def transitionTo(id: String, stateName: String): Unit =
    states += id -> template.states(stateName)

  private def getMethod(accept: Accept): MethodMirror = {
    val methodSymbol = instanceMirror.symbol.info.member(TermName(accept.method)).asMethod
    instanceMirror.reflectMethod(methodSymbol)
  }

  private def transition(id: String, stateName: String, responseDoc: NodeSeq): NodeSeq = {
    val state = template.states(stateName)
    states += id -> state
    val selfDapLink = DapLink("self", template.uriTemplate.replace("{id}", id), None)
    // TODO: add the other DAP links from state.links (fold over a Seq of DapLink ?)
    responseDoc addChild selfDapLink.toXML
  }

  implicit class NodeSeqExtensions(ns: NodeSeq) {
    def addChild(child: Node): NodeSeq =
      ns.headOption match {
        case Some(e: Elem) => e.copy(child = e.child ++ child)
        case other => ns
      }
  }

  implicit class StringExtensions(s: String) {
    def trim(c: Char): String = s.reverse.dropWhile(c => c == '/').reverse
  }
}
