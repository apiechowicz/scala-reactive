package eShop

import akka.actor.Actor
import akka.event.LoggingReceive

object Checkout {

  case class CheckoutTimerExpired() {
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

class Checkout extends Actor {

  import Cart._
  import Checkout._

  override def receive: Receive = LoggingReceive {
    case CheckoutStarted => context become selectingDelivery
  }

  def selectingDelivery: Receive = LoggingReceive {
    case DeliveryMethodSelected => context become selectingPaymentMethod
    case CheckoutCancelled => context become cancelled
  }

  def cancelled: Receive = LoggingReceive {
    case _ => context stop self
  }

  def selectingPaymentMethod: Receive = LoggingReceive {
    case PaymentSelected => context become processingPayment
    case CheckoutCancelled => context become cancelled
  }

  def processingPayment: Receive = LoggingReceive {
    case PaymentReceived => context become closed
    case CheckoutCancelled => context become cancelled
  }

  def closed: Receive = LoggingReceive {
    case _ => context stop self
  }
}
