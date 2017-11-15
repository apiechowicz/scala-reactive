package eShop

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

object Customer {

  case class StartCheckout()

  case class DoPayment()

}

class Customer extends Actor {

  import CartManager._
  import Checkout._
  import Customer._
  import PaymentService._

  val cart: ActorRef = context.actorOf(Props(new CartManager(Cart.empty)), "Cart")

  override def receive: Receive = LoggingReceive {
    case "add" => cart ! ItemAdded
    case "remove" => cart ! ItemRemoved
    case "checkout" =>
      cart ! StartCheckout
      context become inCheckout
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become receive
    case CheckoutStarted(c) =>
      val checkout = c
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected
      context become inPayment
  }

  def inPayment(): Receive = LoggingReceive {
    case PaymentServiceStarted(service) =>
      service ! DoPayment
    case PaymentConfirmed =>
      context become inCheckout
  }
}
