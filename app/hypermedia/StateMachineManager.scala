package hypermedia

class StateMachineManager(private val template: StateMachineTemplate, private val service: Any) {

  import play.api.mvc.Results._
  import play.api.mvc.{Request, Result}

  import scala.xml.{Elem, Node, NodeSeq}
  import scala.reflect.runtime.universe._

  val uriTemplate: String = template.uriTemplate

  private var states: Map[String, State] = Map()
  private val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(service)

  def transitionTo(id: String, stateName: String): Unit =
    states += id -> template.states(stateName)

  def process(request: Request[NodeSeq]): Result = {

    val pos = uriTemplate indexOf "/{"
    val baseUri = uriTemplate take pos
    val trimmedRequestUri = request.uri.trim('/')

    template.initialState.accepts find (a => a.httpVerb == request.method) match {
      case Some(accept) if trimmedRequestUri == baseUri =>
        commonHandling1(request, template.initialState, accept, None)
      case _ =>
        val id = request.uri.drop(pos + 1)
        val maybeResult = for {
          currentState <- states.get(id)
          accept <- currentState.accepts find (a => a.httpVerb == request.method)
        } yield commonHandling1(request, currentState, accept, Some(id))
        maybeResult getOrElse InternalServerError("TODO: add error message...")
    }
  }

  private def commonHandling1(request: Request[NodeSeq], state: State, accept: Accept, maybeId: Option[String]): Result = {
    val methodMirror = getMethod(accept)
    val result = maybeId match {
      case Some(id) => methodMirror(id, request.body)
      case None => methodMirror(request.body)
    }
    (maybeId, result) match {
      case (Some(id), responseDoc: NodeSeq) => commonHandling2(id, responseDoc, state, accept)
      case (None, (id: String, responseDoc: NodeSeq)) => commonHandling2(id, responseDoc, state, accept)
      case other => throw new Exception(s"accept method returned unexpected value, $other")
    }
  }

  // TODO: handle accept.errors
  // TODO: handle other errors => 500
  private def commonHandling2(id: String, responseDoc: NodeSeq, state: State, accept: Accept): Result = {
    val newStateName = accept.transitionTo.getOrElse(state.name)
    val statusCode = accept.response
    new Status(statusCode).apply(transition(id, newStateName, responseDoc))
  }

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
