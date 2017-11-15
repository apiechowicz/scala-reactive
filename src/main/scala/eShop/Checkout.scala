package eShop

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props, Timers}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration.FiniteDuration

object Checkout {

  private case class CheckoutTimerExpired()

  case class DeliveryMethodSelected()

  case class PaymentSelected(date: Long = System.currentTimeMillis())

  private case class PaymentTimerExpired()

  case class PaymentServiceStarted(service: ActorRef)

}

class Checkout(id: Long) extends PersistentActor with Timers {

  import CartManager._
  import Checkout._
  import PaymentService.PaymentReceived

  val checkoutTimer = "CheckoutTimer"
  val checkoutTimeout = FiniteDuration(30, TimeUnit.SECONDS)
  val paymentTimer = "PaymentTimer"
  val paymentTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  override def persistenceId: String = "checkout#" + id

  override def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case event: Any => updateState(event)
  }

  override def receiveCommand: Receive = LoggingReceive {
    case event: CheckoutStarted => persist(event)(event => updateState(event))
  }

  // state definitions:

  def selectingDelivery: Receive = LoggingReceive {
    case DeliveryMethodSelected => persist(DeliveryMethodSelected)(event => updateState(event))
    case CheckoutCancelled => persist(CheckoutCancelled)(event => updateState(event))
    case CheckoutTimerExpired => persist(CheckoutTimerExpired)(event => updateState(event))
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case event: PaymentSelected => persist(event) { event =>
      updateState(event)
      sender ! PaymentServiceStarted(context.actorOf(Props[PaymentService], "PaymentMethod"))
    }
    case CheckoutCancelled => persist(CheckoutCancelled)(event => updateState(event))
    case PaymentTimerExpired => persist(PaymentTimerExpired)(event => updateState(event))
  }

  def processingPayment: Receive = LoggingReceive {
    case PaymentReceived => persist(PaymentReceived) { event =>
      updateState(event)
      context.parent ! CheckoutClosed
    }
    case CheckoutCancelled => persist(CheckoutCancelled)(event => updateState(event))
    case PaymentTimerExpired => persist(PaymentTimerExpired)(event => updateState(event))
  }

  // end of state definitions

  private def updateState(event: Any): Unit = {
    event match {
      case event: CheckoutStarted =>
        context become selectingDelivery
        startTimer(event.date, checkoutTimer, checkoutTimeout, CheckoutTimerExpired)
      case DeliveryMethodSelected => context become selectingPaymentMethod
      case CheckoutCancelled =>
        timers.cancelAll()
        context stop self
      case CheckoutTimerExpired =>
        timers.cancel(checkoutTimer)
        context stop self
      case event: PaymentSelected =>
        timers.cancel(checkoutTimer)
        startTimer(event.date, paymentTimer, paymentTimeout, PaymentTimerExpired)
        context become processingPayment
      case PaymentTimerExpired =>
        timers.cancel(paymentTimer)
        context stop self
      case PaymentReceived =>
        timers.cancel(paymentTimer)
        context stop self
    }
  }

  private def startTimer(starTime: Long, timer: String, timeout: FiniteDuration, event: Any): Unit = {
    val timeLeft = (starTime + timeout.toMillis) - System.currentTimeMillis()
    if (timeLeft > 0) timers.startSingleTimer(timer, event, FiniteDuration(timeLeft, TimeUnit.MILLISECONDS))
    else updateState(event)
  }
}
