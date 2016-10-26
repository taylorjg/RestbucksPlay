package api

import javax.inject.Inject

import hypermedia.{OrderTemplate, PaymentTemplate, StateMachineDispatcher, StateMachineManager}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import services.{InMemoryDatabaseService, OrderingService}

class ApiRouter @Inject() extends SimpleRouter {

  private val orderingService = new OrderingService(new InMemoryDatabaseService)

  private val stateMachineDispatcher = new StateMachineDispatcher(
    new StateMachineManager(OrderTemplate.template, orderingService),
    new StateMachineManager(PaymentTemplate.template, orderingService))

  override def routes: Routes = {
    case _ => stateMachineDispatcher.dispatch
  }
}
