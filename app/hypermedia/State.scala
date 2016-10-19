package hypermedia

case class State(name: String, transitionTo: String, accepts: Seq[Accept], links: Seq[Link])
