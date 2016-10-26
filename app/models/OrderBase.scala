package models

trait OrderBase {
  def location: String
  def items: Seq[OrderItem]
}
