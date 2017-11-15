package eShop

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object CartManager {

  case class ItemAdded(item: Item)

  case class ItemRemoved(item: Item, count: Int)

  private case class CartTimerExpired()

  case class CheckoutStarted(actor: ActorRef)

  case class CheckoutCancelled()

  case class CheckoutClosed()

  case class CartEmpty()

}

class CartManager(var cart: Cart) extends Actor with Timers {

  import CartManager._
  import Customer._

  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(5, TimeUnit.MINUTES)

  override def receive: Receive = empty()

  def empty(): Receive = LoggingReceive {
    case ItemAdded(item) =>
      cart = cart.addItem(item)
      startTimer()
      context become nonEmpty
  }

  private def startTimer(): Unit = {
    timers.startSingleTimer(cartTimer, CartTimerExpired, cartTimeout)
  }

  def nonEmpty(): Receive = LoggingReceive {
    case ItemAdded(item) =>
      cart = cart.addItem(item)
      startTimer()
    case ItemRemoved(item, count) =>
      cart = cart.removeItem(item, count)
      if (!cart.isEmpty) startTimer()
      else {
        cancelTimer()
        becomeEmpty(sender)
      }
    case StartCheckout =>
      cancelTimer()
      val checkout = context.actorOf(Props[Checkout], "Checkout")
      sender ! CheckoutStarted(checkout)
      checkout ! CheckoutStarted(context.parent)
      context become inCheckout
    case CartTimerExpired =>
      cart = Cart.empty
      becomeEmpty(sender)
  }

  private def cancelTimer(): Unit = {
    timers.cancel(cartTimer)
  }

  private def becomeEmpty(sender: ActorRef): Unit = {
    sender ! CartEmpty
    context become empty
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed =>
      becomeEmpty(context.parent)
    case CheckoutCancelled =>
      startTimer()
      context become nonEmpty
  }
}
