package hypermedia

case class Link(rel: String, resource: Option[String] = None)
