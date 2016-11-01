package services

import models.{OrderItem, OrderRequest}

object CostCalculatorService {

  def calculateCost(orderRequest: OrderRequest): Double =
    orderRequest.items.foldLeft(0.toDouble)((acc, orderItem) => acc + costOfItem(orderItem))

  private def costOfItem(orderItem: OrderItem): Double = {
    val drinkPrice = orderItem.coffee match {
      case "coffee" => 5.0
      case "latte" => 4.5
      case "espresso" => 4.5
      case "cappuccino" => 4.5
      case "flatWhite" => 5.5
      case _ => throw new Exception(s"Unknown drink: ${orderItem.coffee}")
    }
    val sizeMultiplier = orderItem.size match {
      case "small" => 0.5
      case "medium" => 1.0
      case "large" => 1.5
      case _ => throw new Exception(s"Unknown drink size: ${orderItem.size}")
    }
    drinkPrice * sizeMultiplier
  }
}
