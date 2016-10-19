package api

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

class ApiRouter @Inject() extends SimpleRouter {
  override def routes: Routes = {
    case request => Action {
      Ok(Json.toJson(Map("method" -> request.method, "uri" -> request.uri)))
    }
  }
}
