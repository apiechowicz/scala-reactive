package eShop

import akka.actor.Actor
import akka.dispatch.sysmsg.Failed
import akka.event.LoggingReceive
import eShop.Cart._
import eShop.Checkout.{DeliveryMethodSelected, PaymentReceived, PaymentSelected}

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
  override def receive: LoggingReceive = {
    case CheckoutStarted => context become selectingDelivery
    case _ => Failed
  }

  def selectingDelivery: LoggingReceive = {
    case DeliveryMethodSelected => context become selectingPaymentMethod
    case CheckoutCancelled => context become cancelled
    case _ => Failed
  }

  def cancelled: LoggingReceive = {
    case _ => context stop self
  }

  def selectingPaymentMethod: LoggingReceive = {
    case PaymentSelected => context become processingPayment
    case CheckoutCancelled => context become cancelled
    case _ => Failed
  }

  def processingPayment: LoggingReceive = {
    case PaymentReceived => context become closed
    case CheckoutCancelled => context become cancelled
    case _ => Failed
  }

  def closed: LoggingReceive = {
    case _ => context stop self
  }
}
