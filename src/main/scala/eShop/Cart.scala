package eShop

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object Cart {

  case class ItemAdded() {
  }

  case class ItemRemoved() {
  }

  private case class CartTimerExpired() {
  }

  case class CheckoutStarted() {
  }

  case class CheckoutCancelled() {
  }

  case class CheckoutClosed() {
  }

}

class Cart() extends Actor with Timers {

  import Cart._

  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  var itemCount = BigDecimal(0)

  override def receive: Receive = empty()

  def empty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      timers.startSingleTimer(cartTimer, CartTimerExpired, cartTimeout)
      context become nonEmpty
  }

  def nonEmpty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      timers.startSingleTimer(cartTimer, CartTimerExpired, cartTimeout)
    case ItemRemoved =>
      if (itemCount > 1) {
        itemCount -= 1
        timers.startSingleTimer(cartTimer, CartTimerExpired, cartTimeout)
      } else {
        itemCount = 0
        timers.cancel(cartTimer)
        context become empty
      }
    case CheckoutStarted =>
      timers.cancel(cartTimer)
      context become inCheckout
    case CartTimerExpired =>
      itemCount = 0
      context become empty
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become empty
    case CheckoutCancelled =>
      timers.cancel(cartTimer)
      context become nonEmpty
  }
}
