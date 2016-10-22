package hypermedia

case class State(name: String, transitionTo: Option[String], accepts: Seq[Accept], links: Seq[Link])
