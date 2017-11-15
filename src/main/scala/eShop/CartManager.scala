package eShop

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration.FiniteDuration

object CartManager {

  case class ItemAdded(item: Item, date: Long)

  case class ItemRemoved(item: Item, count: Int, date: Long)

  private case class CartTimerExpired()

  case class CheckoutStarted(actor: ActorRef)

  case class CheckoutCancelled(date: Long = System.currentTimeMillis())

  case class CheckoutClosed()

  case class CartEmpty()

}

class CartManager(id: Long, var cart: Cart) extends PersistentActor with Timers {

  import CartManager._
  import Customer._

  val cartTimer = "CartTimer"
  val cartTimeout = FiniteDuration(5, TimeUnit.MINUTES)

  override def persistenceId: String = "cart_manager#" + id

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case event: Any => updateState(event)
  }

  override def receiveCommand: Receive = empty()

  // state definitions:
  def empty(): Receive = LoggingReceive {
    case event: ItemAdded => persist(event)(event => updateState(event))
  }

  def nonEmpty(): Receive = LoggingReceive {
    case event: ItemAdded => persist(event)(event => updateState(event))
    case event: ItemRemoved => persist(event) { event =>
      updateState(event)
      if (cart.isEmpty) sender ! CartEmpty
    }
    case StartCheckout => persist(StartCheckout) { event =>
      updateState(event)
      val checkout = context.actorOf(Props[Checkout], "Checkout")
      sender ! CheckoutStarted(checkout)
      checkout ! CheckoutStarted(context.parent)
    }
    case CartTimerExpired => persist(CartTimerExpired) { event =>
      updateState(event)
      sender ! CartEmpty
    }
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => persist(CheckoutClosed) { event =>
      updateState(event)
      sender ! CartEmpty
    }
    case CheckoutCancelled => persist(CheckoutCancelled)(event => updateState(event))
  }

  // end of state definitions

  private def updateState(event: Any): Unit = {
    event match {
      case ItemAdded(item, date) =>
        if (cart.isEmpty) context become nonEmpty
        cart = cart.addItem(item)
        startTimer(date)
      case ItemRemoved(item, count, date) =>
        cart = cart.removeItem(item, count)
        if (!cart.isEmpty) startTimer(date)
        else {
          cancelTimer()
          context become empty
        }
      case StartCheckout =>
        cancelTimer()
        context become inCheckout
      case CartTimerExpired =>
        cart = Cart.empty
        context become empty
      case CheckoutClosed =>
        context become empty
      case CheckoutCancelled(date) =>
        startTimer(date)
        context become nonEmpty
    }
  }

  private def startTimer(starTime: Long): Unit = {
    val timeLeft = (starTime + cartTimeout.toMillis) - System.currentTimeMillis()
    if (timeLeft > 0) timers.startSingleTimer(cartTimer, CartTimerExpired, FiniteDuration(timeLeft, TimeUnit.MILLISECONDS))
    else updateState(CartTimerExpired)
  }

  private def cancelTimer(): Unit = {
    timers.cancel(cartTimer)
  }
}
