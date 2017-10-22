package eShop

import akka.actor._
import akka.dispatch.sysmsg.Failed
import akka.event.LoggingReceive
import eShop.Cart._

object Cart {

  case class ItemAdded() {
  }

  case class ItemRemoved() {
  }

  case class CartTimerExpired() {
  }

  case class CheckoutStarted() {
  }

  case class CheckoutCancelled() {
  }

  case class CheckoutClosed() {
  }

}

class Cart() extends Actor {
  var itemCount = BigDecimal(0)

  override def receive: LoggingReceive = empty()

  def empty(): LoggingReceive = {
    case ItemAdded =>
      itemCount += 1
      context become nonEmpty
    case _ => sender ! Failed
  }

  def nonEmpty(): LoggingReceive = {
    case ItemAdded => itemCount += 1
    case ItemRemoved =>
      itemCount -= 1
      if (itemCount == BigDecimal(0)) {
        context become empty
      }
    case CheckoutStarted => context become inCheckout
    case _ => sender ! Failed
  }

  def inCheckout(): LoggingReceive = {
    case CheckoutClosed => context become empty
    case CheckoutCancelled => context become nonEmpty
    case _ => sender ! Failed
  }
}
