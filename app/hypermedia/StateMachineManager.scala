package hypermedia

import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

class StateMachineManager(private val template: StateMachineTemplate) {

  val uriTemplate: String = template.uriTemplate

  def process(request: RequestHeader): Result =
    Ok(Json.toJson(Map(
      "method" -> request.method,
      "uri" -> request.uri)))
}
