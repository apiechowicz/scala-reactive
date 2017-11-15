package eShop

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object Checkout {

  private case class CheckoutTimerExpired()

  case class DeliveryMethodSelected()

  case class PaymentSelected()

  private case class PaymentTimerExpired()

  case class PaymentServiceStarted(service: ActorRef)

}

class Checkout extends Actor with Timers {

  import CartManager._
  import Checkout._
  import PaymentService.PaymentReceived

  val checkoutTimer = "CheckoutTimer"
  val checkoutTimeout = FiniteDuration(30, TimeUnit.SECONDS)
  val paymentTimer = "PaymentTimer"
  val paymentTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  override def receive: Receive = LoggingReceive {
    case CheckoutStarted(_) =>
      timers.startSingleTimer(checkoutTimer, CheckoutTimerExpired, checkoutTimeout)
      context become selectingDelivery
  }

  def selectingDelivery: Receive = LoggingReceive {
    case DeliveryMethodSelected => context become selectingPaymentMethod
    case CheckoutCancelled =>
      timers.cancel(checkoutTimer)
      context become cancelled
    case CheckoutTimerExpired => context become cancelled
  }

  def cancelled: Receive = LoggingReceive {
    case _ => context stop self
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case PaymentSelected =>
      timers.cancel(checkoutTimer)
      sender ! PaymentServiceStarted(context.actorOf(Props[PaymentService], "PaymentMethod"))
      timers.startSingleTimer(paymentTimer, PaymentTimerExpired, FiniteDuration(500, TimeUnit.MILLISECONDS))
      context become processingPayment
    case CheckoutCancelled =>
      timers.cancel(checkoutTimer)
      context become cancelled
    case PaymentTimerExpired => context become cancelled
  }

  def processingPayment: Receive = LoggingReceive {
    case PaymentReceived =>
      timers.cancel(paymentTimer)
      context.parent ! CheckoutClosed
      context stop self
    case CheckoutCancelled =>
      timers.cancel(paymentTimer)
      context become cancelled
    case PaymentTimerExpired => context become cancelled
  }
}
