package hypermedia

import play.api.libs.json.Json
import play.api.mvc.{Action, Handler, RequestHeader}
import play.api.mvc.Results._

// Implement this as an Akka actor ?
class StateMachineDispatcher {

  private var stateMachines: Seq[StateMachineManager] = Seq()

  def add(stateMachine: StateMachineManager) =
    stateMachines = stateMachines :+ stateMachine

  def dispatch(request: RequestHeader): Handler = Action {
    Ok(Json.toJson(
      Map(
        "method" -> request.method,
        "uri" -> request.uri)))
  }
}
