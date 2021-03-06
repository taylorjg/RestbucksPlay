package hypermedia

import services.DatabaseService

class StateMachineManager(val resourceName: String,
                          private val schema: String,
                          private val template: StateMachineTemplate,
                          private val db: DatabaseService,
                          private val service: Any) {

  import ops.NodeSeqOps._
  import ops.StringOps._
  import play.api.mvc.Results._
  import play.api.mvc.{RawBuffer, Request, RequestHeader, Result}

  import scala.reflect.runtime.universe._
  import scala.util.{Failure, Success, Try}
  import scala.xml.{NodeSeq, SAXParseException}

  val uriTemplate = template.uriTemplate

  private val instanceMirror = runtimeMirror(getClass.getClassLoader).reflect(service)

  def createResource(id: String): Unit = {
    def followStateTransitionTo(state: State): State =
      state.transitionTo match {
        case Some(stateName) => followStateTransitionTo(template.states(stateName))
        case None => state
      }
    val state = followStateTransitionTo(template.initialState)
    transitionTo(id, state.name)
  }

  def transitionTo(id: String, stateName: String): Unit = {
    val states = db.loadStatesMap(uriTemplate)
    val newState = template.states(stateName)
    db.saveStatesMap(uriTemplate, states.updated(id, newState))
  }

  def process(stateMachineManagers: Map[String, StateMachineManager],
              request: Request[RawBuffer]): Result = {

    val tryResult = Try {

      val tryRequestDoc = request.method match {
        case "GET" | "HEAD" | "DELETE" => Success(NodeSeq.Empty)
        case _ =>
          val charset = request.charset getOrElse "utf-8"
          val xml = request.body.asBytes().get.decodeString(charset)
          ops.LoadXmlWithSchema(xml, schema)
      }

      tryRequestDoc match {
        case Success(requestDoc) =>

          val pos = uriTemplate indexOf "/{"
          val baseUri = uriTemplate take pos
          val trimmedRequestUri = request.uri.trim('/')

          template.initialState.accepts find (a => a.httpVerb == request.method) match {
            case Some(accept) if trimmedRequestUri == baseUri =>
              commonHandling1(stateMachineManagers, request, requestDoc, template.initialState, accept, None)
            case _ =>
              val id = request.uri.drop(pos + 1)
              db.loadStatesMap(uriTemplate).get(id) match {
                case Some(currentState) =>
                  val maybeResult = for {
                    accept <- currentState.accepts find (a => a.httpVerb == request.method)
                  } yield commonHandling1(stateMachineManagers, request, requestDoc, currentState, accept, Some(id))
                  maybeResult getOrElse MethodNotAllowed
                case None => NotFound
              }
          }
        case Failure(ex: SAXParseException) => BadRequest(ex.getMessage)
        case Failure(ex) => InternalServerError(ex.getMessage)
      }
    }

    tryResult match {
      case Success(result) => result
      case Failure(ex) =>
        val maybeMessage1 = Option(ex.getMessage)
        val maybeCause = Option(ex.getCause)
        val maybeMessage2 = maybeCause map (_.getMessage)
        val message = maybeMessage1 orElse maybeMessage2 getOrElse ""
        InternalServerError(message)
    }
  }

  private def commonHandling1(stateMachineManagers: Map[String, StateMachineManager],
                              request: RequestHeader,
                              requestDoc: NodeSeq,
                              state: State,
                              accept: Accept,
                              maybeId: Option[String]): Result = {
    val baseUri = "http" + (if (request.secure) "s" else "") + "://" + request.host
    val methodMirror = getMethod(accept)
    val result = maybeId match {
      case Some(id) => methodMirror(stateMachineManagers, requestDoc, id)
      case None => methodMirror(stateMachineManagers, requestDoc)
    }
    (maybeId, result) match {
      case (Some(id), Right(responseDoc: NodeSeq)) => commonHandling2(baseUri, id, responseDoc, state, accept)
      case (None, Right((id: String, responseDoc: NodeSeq))) => commonHandling2(baseUri, id, responseDoc, state, accept)
      case (_, Left(result: Result)) => result
      case other => InternalServerError(s"Service method returned unexpected value, $other")
    }
  }

  private def commonHandling2(baseUri: String, id: String, responseDoc: NodeSeq, state: State, accept: Accept): Result = {
    val newStateName = accept.transitionTo.getOrElse(state.name)
    val statusCode = accept.response
    new Status(statusCode).apply(transition(baseUri, id, newStateName, responseDoc))
  }

  private def getMethod(accept: Accept): MethodMirror = {
    val methodSymbol = instanceMirror.symbol.info.member(TermName(accept.method)).asMethod
    instanceMirror.reflectMethod(methodSymbol)
  }

  private def absUri(baseUri: String, path: String): String = s"$baseUri$path"

  private def transition(baseUri: String, id: String, stateName: String, responseDoc: NodeSeq): NodeSeq = {

    val state = template.states(stateName)
    val states = db.loadStatesMap(uriTemplate)
    db.saveStatesMap(uriTemplate, states.updated(id, state))

    if (state.links.nonEmpty) {
      val selfDapLink = DapLink("self", absUri(baseUri, template.uriTemplate.replace("{id}", id)), None)
      val otherDapLinks = state.links map (link => {
        val rel = s"${template.relationsIn.trim('/')}/${link.rel}"
        val uri = absUri(baseUri, (link.resource getOrElse uriTemplate).replace("{id}", id))
        DapLink(rel, uri, Some(template.mediaType))
      })
      val dapLinks = selfDapLink +: otherDapLinks
      dapLinks.foldLeft(responseDoc)((currentResponseDoc, dapLink) => currentResponseDoc addChild dapLink.toXML)
    }
    else responseDoc
  }
}
