package api

import javax.inject.Inject

import hypermedia.{OrderTemplate, PaymentTemplate, StateMachineDispatcher, StateMachineManager}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import services.{DatabaseService, OrderingService}

class ApiRouter @Inject()(db: DatabaseService) extends SimpleRouter {

  private val orderingService = new OrderingService(db)

  private val stateMachineDispatcher = new StateMachineDispatcher(
    new StateMachineManager(OrderTemplate.template, db, orderingService),
    new StateMachineManager(PaymentTemplate.template, db, orderingService))

  override def routes: Routes = {
    case _ => stateMachineDispatcher.dispatch
  }
}
