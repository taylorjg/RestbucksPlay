package modules

import com.google.inject.AbstractModule

class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    import services._
    bind(classOf[DatabaseService]).to(classOf[InMemoryDatabaseService])
  }
}
