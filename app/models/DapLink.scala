package models

case class DapLink(rel: String, uri: String, mediaType: Option[String])

object DapLink {

  import scala.xml.Node

  implicit class DapLinkExtensions(dapLink: DapLink) {
    // TODO: handle DAP namespace - "http://schemas.restbucks.com/dap"
    def toXML: Node =
      dapLink.mediaType match {
        case Some(mediaType) => <link rel={dapLink.rel} uri={dapLink.uri} mediaType={mediaType}></link>
        case None => <link rel={dapLink.rel} uri={dapLink.uri}></link>
      }
  }
}
