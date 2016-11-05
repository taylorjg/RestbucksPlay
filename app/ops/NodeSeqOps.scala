package ops

import java.io._
import java.nio.charset.StandardCharsets
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.xml.sax.InputSource

import scala.util.{Success, Try}
import scala.xml.{Elem, Node, NodeSeq}

object NodeSeqOps {

  implicit class NodeSeqOps(ns: NodeSeq) {
    def addChild(child: Node): NodeSeq =
      ns.headOption match {
        case Some(e: Elem) => e.copy(child = e.child ++ child)
        case other => ns
      }
  }

}

object LoadXmlWithSchema {
  def apply(xml: String, schemaResource: String): Try[Elem] =
    Try {
      val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
      val schema = schemaFactory.newSchema(new StreamSource(getClass.getClassLoader.getResourceAsStream(schemaResource)))
      val xmlStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
      val xmlInputSource = new InputSource(xmlStream)
      new SchemaAwareFactoryAdapter(schema).loadXML(xmlInputSource)
    }
}
