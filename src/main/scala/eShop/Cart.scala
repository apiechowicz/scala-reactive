package eShop

import akka.actor._

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
  /*
  states:
  Empty, NonEmpty, InCheckout
   */
  override def receive = ???
}
