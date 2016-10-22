package api

import javax.inject.Inject

import hypermedia.{OrderTemplate, PaymentTemplate, StateMachineDispatcher, StateMachineManager}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

class ApiRouter @Inject() extends SimpleRouter {

  private val stateMachineDispatcher = new StateMachineDispatcher(
    new StateMachineManager(OrderTemplate.template),
    new StateMachineManager(PaymentTemplate.template))

  override def routes: Routes = {
    case request => stateMachineDispatcher.dispatch(request)
  }
}
