package hypermedia

import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

class StateMachineManager(private val template: StateMachineTemplate) {

  val uriTemplate: String = template.uriTemplate

  def process(request: RequestHeader): Result =
    Ok(
      <response>
        <method>{request.method}</method>
        <uri>{request.uri}</uri>
        <headers>
          { request.headers.headers map {
              case (n, v) => {
                <header>
                  <name>{n}</name>
                  <value>{v}</value>
                </header>
              }
            }
          }
        </headers>
      </response>)
}
