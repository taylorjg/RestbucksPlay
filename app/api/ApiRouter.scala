package api

import javax.inject.Inject

import hypermedia.StateMachineDispatcher
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

class ApiRouter @Inject() extends SimpleRouter {

  private val stateMachineDispatcher = new StateMachineDispatcher

  override def routes: Routes = {
    case request => stateMachineDispatcher.dispatch(request)
  }
}
