package hypermedia

import play.api.mvc.Results._
import play.api.mvc.{Action, Handler, RequestHeader}

class StateMachineDispatcher(private val stateMachines: StateMachineManager*) {

  def dispatch(request: RequestHeader): Handler = Action {

    val stateMachine = stateMachines find {
      sm =>
        val pos = sm.uriTemplate indexOf "/{"
        val prefix = sm.uriTemplate take pos
        request.uri startsWith prefix
    }

    stateMachine.fold {
      NotFound(s"The dispatcher couldn't find a state machine for: ${request.uri}")
    } {
      sm => sm.process(request)
    }
  }
}
