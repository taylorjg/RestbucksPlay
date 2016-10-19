package hypermedia

case class Accept(httpVerb: String, method: String, response: Int, transitionTo: String, errors: Seq[Error])
