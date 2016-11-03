package ops

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
