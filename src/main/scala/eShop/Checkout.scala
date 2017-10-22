package eShop

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration

object Checkout {

  private case class CheckoutTimerExpired() {
  }

  case class DeliveryMethodSelected() {
  }

  case class PaymentSelected() {
  }

  case class PaymentReceived() {
  }

  private case class PaymentTimerExpired() {
  }

}

class Checkout extends Actor with Timers {

  import Cart._
  import Checkout._

  val checkoutTimer = "CheckoutTimer"
  val checkoutTimeout = FiniteDuration(30, TimeUnit.SECONDS)
  val paymentTimer = "PaymentTimer"
  val paymentTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  override def receive: Receive = LoggingReceive {
    case CheckoutStarted =>
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
      context become closed
    case CheckoutCancelled =>
      timers.cancel(paymentTimer)
      context become cancelled
    case PaymentTimerExpired => context become cancelled
  }

  def closed: Receive = LoggingReceive {
    case _ => context stop self
  }
}
