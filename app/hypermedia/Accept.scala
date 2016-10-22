package hypermedia

case class Accept(httpVerb: String, method: String, response: Int, transitionTo: Option[String], errors: Seq[Error])
