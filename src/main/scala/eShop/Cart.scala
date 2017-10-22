package eShop

import akka.actor._
import akka.event.LoggingReceive

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

  import Cart._

  var itemCount = BigDecimal(0)

  override def receive: Receive = empty()

  def empty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      context become nonEmpty
  }

  def nonEmpty(): Receive = LoggingReceive {
    case ItemAdded => itemCount += 1
    case ItemRemoved =>
      itemCount -= 1
      if (itemCount == BigDecimal(0)) {
        context become empty
      }
    case CheckoutStarted => context become inCheckout
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become empty
    case CheckoutCancelled => context become nonEmpty
  }
}
