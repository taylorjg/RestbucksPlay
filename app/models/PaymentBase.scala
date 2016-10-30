package models

trait PaymentBase {
  def amount: Double
  def cardHolder: String
  def cardNumber: String
  def expiryMonth: Int
  def expiryYear: Int
}
