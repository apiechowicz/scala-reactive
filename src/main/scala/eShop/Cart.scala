package eShop

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object Cart {

  case class ItemAdded()

  case class ItemRemoved()

  private case class CartTimerExpired()

  case class CheckoutStarted(actor: ActorRef)

  case class CheckoutCancelled()

  case class CheckoutClosed()

  case class CartEmpty()

}

class Cart() extends Actor with Timers {

  import Cart._
  import Customer._

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
        becomeEmpty(sender)
      }
    case StartCheckout =>
      timers.cancel(cartTimer)
      val checkout = context.actorOf(Props[Checkout], "Checkout")
      sender ! CheckoutStarted(checkout)
      checkout ! CheckoutStarted(context.parent)
      context become inCheckout
    case CartTimerExpired =>
      itemCount = 0
      becomeEmpty(sender)
  }

  private def becomeEmpty(sender: ActorRef): Unit = {
    sender ! CartEmpty
    context become empty
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become empty
    case CheckoutCancelled =>
      timers.cancel(cartTimer)
      context become nonEmpty
  }
}
