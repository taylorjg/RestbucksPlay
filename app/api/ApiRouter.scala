package api

import javax.inject.Inject

import hypermedia.{OrderTemplate, PaymentTemplate, StateMachineDispatcher, StateMachineManager}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import services.{DatabaseService, OrderingService}

class ApiRouter @Inject()(db: DatabaseService) extends SimpleRouter {

  private val orderingService = new OrderingService(db)
  private val orderStateMachineManager = new StateMachineManager(OrderTemplate.template, db, orderingService)
  private val paymentStateMachineManager = new StateMachineManager(PaymentTemplate.template, db, orderingService)
  private val stateMachineDispatcher = new StateMachineDispatcher(orderStateMachineManager, paymentStateMachineManager)

  private val stateMachineManagers = Map(
    "order" -> orderStateMachineManager,
    "payment" -> paymentStateMachineManager)

  override def routes: Routes = {
    case _ => stateMachineDispatcher.dispatch(stateMachineManagers)
  }
}
