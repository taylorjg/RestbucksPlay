package hypermedia

import play.api.mvc.Results._
import play.api.mvc.{Action, Handler}

class StateMachineDispatcher(private val stateMachines: StateMachineManager*) {

  def dispatch: Handler = Action { request =>

    val requestDoc = request.method match {
      case "GET" | "HEAD" => None
      case _ => request.body.asXml
    }

    val stateMachine = stateMachines find { sm =>
      val pos = sm.uriTemplate indexOf "/{"
      val baseUri = sm.uriTemplate take pos
      request.uri startsWith baseUri
    }

    stateMachine.fold {
      NotFound(s"The dispatcher couldn't find a state machine for: ${request.uri}")
    } {
      sm => sm.process(request, requestDoc)
    }
  }
}
