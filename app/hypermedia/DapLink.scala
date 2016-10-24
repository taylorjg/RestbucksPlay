package hypermedia

case class DapLink(rel: String, uri: String, mediaType: Option[String])

object DapLink {

  import scala.xml.Node

  def fromXML(node: Node): DapLink = {
    val rel = node \@ "rel"
    val uri = node \@ "uri"
    val maybeMediaType = node attribute "mediaType" match {
      case Some(mediaTypeAttribute) => Some(mediaTypeAttribute.text)
      case _ => None
    }
    DapLink(rel, uri, maybeMediaType)
  }

  implicit class DapLinkExtensions(dapLink: DapLink) {
    // TODO: handle DAP namespace - "http://schemas.restbucks.com/dap"
    def toXML: Node =
      dapLink.mediaType match {
        case Some(mediaType) => <link rel={dapLink.rel} uri={dapLink.uri} mediaType={mediaType}></link>
        case None => <link rel={dapLink.rel} uri={dapLink.uri}></link>
      }
  }
}
