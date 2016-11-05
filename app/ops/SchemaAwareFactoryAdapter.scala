package ops

import javax.xml.parsers.{SAXParser, SAXParserFactory}
import javax.xml.validation.Schema

import scala.xml._
import scala.xml.parsing._

/** Enables validation of xml on loading
  *
  * @param schema The XML Schema to validate any xml loaded by this SchemaAwareFactoryAdapter
  * @see http://sean8223.blogspot.com/2009/09/xsd-validation-in-scala.html
  */
class SchemaAwareFactoryAdapter(schema: Schema) extends NoBindingFactoryAdapter {

  def loadXML(source: InputSource): Elem = {

    val parser: SAXParser = try {
      val f = SAXParserFactory.newInstance()
      f.setNamespaceAware(true)
      f.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
      f.newSAXParser()
    } catch {
      case e: Exception =>
        throw e
    }

    val xr = parser.getXMLReader
    val vh = schema.newValidatorHandler()
    vh.setContentHandler(this)
    xr.setContentHandler(vh)

    scopeStack.push(TopScope)
    xr.parse(source)
    scopeStack.pop
    rootElem.asInstanceOf[Elem]
  }
}
