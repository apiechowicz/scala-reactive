package eShop

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object CartManager {

  case class ItemAdded()

  case class ItemRemoved()

  private case class CartTimerExpired()

  case class CheckoutStarted(actor: ActorRef)

  case class CheckoutCancelled()

  case class CheckoutClosed()

  case class CartEmpty()

}

class CartManager extends Actor with Timers {

  import CartManager._
  import Customer._

  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(5, TimeUnit.MINUTES)

  var itemCount = BigDecimal(0)

  override def receive: Receive = empty()

  def empty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      startTimer()
      context become nonEmpty
  }

  private def startTimer(): Unit = {
    timers.startSingleTimer(cartTimer, CartTimerExpired, cartTimeout)
  }

  def nonEmpty(): Receive = LoggingReceive {
    case ItemAdded =>
      itemCount += 1
      startTimer()
    case ItemRemoved if itemCount > 1 =>
      itemCount -= 1
      startTimer()
    case ItemRemoved =>
      itemCount = 0
      cancelTimer()
      becomeEmpty(sender)
    case StartCheckout =>
      cancelTimer()
      val checkout = context.actorOf(Props[Checkout], "Checkout")
      sender ! CheckoutStarted(checkout)
      checkout ! CheckoutStarted(context.parent)
      context become inCheckout
    case CartTimerExpired =>
      itemCount = 0
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
