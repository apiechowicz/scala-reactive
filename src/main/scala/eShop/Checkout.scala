package eShop

import akka.actor.Actor

object Checkout {

  case class CheckoutTimerExpired() {
  }

  case class DeliveryMethodSelected() {
  }

  case class PaymentSelected() {
  }

  case class PaymentReceived() {
  }

  case class PaymentTimerExpired() {
  }

}

class Checkout extends Actor {
  /*
  states:
  Selecting, Delivery, Cancelled, SelectingPaymentMethod, ProcessingPayment, Closed
  */
  override def receive = ???
}
