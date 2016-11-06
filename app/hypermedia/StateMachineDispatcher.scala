package hypermedia

class StateMachineDispatcher(private val stateMachines: StateMachineManager*) {

  import play.api.mvc.BodyParsers.parse
  import play.api.mvc.Results.NotFound
  import play.api.mvc.{Action, Handler}

  def dispatch(stateMachineManagers: Map[String, StateMachineManager]): Handler = Action(parse.raw) { request =>

    val stateMachine = stateMachines find { sm =>
      val pos = sm.uriTemplate indexOf "/{"
      val baseUri = sm.uriTemplate take pos
      request.uri startsWith baseUri
    }

    stateMachine.fold {
      NotFound(s"The dispatcher couldn't find a state machine for: ${request.uri}")
    } {
      sm => sm.process(stateMachineManagers, request)
    }
  }
}
