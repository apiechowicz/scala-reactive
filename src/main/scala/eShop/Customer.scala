package eShop

import java.net.URI

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

  val cartId: Long = 1
  /*Random.nextLong()*/
  val cartManager: ActorRef = context.actorOf(Props(new CartManager(cartId, Cart.empty)), "CartManager")

  override def receive: Receive = LoggingReceive {
    case "add" => cartManager ! ItemAdded(Item(URI.create("itemName"), "itemName", BigDecimal.apply(10), 1), System.currentTimeMillis())
    case "remove" => cartManager ! ItemRemoved(Item(URI.create("itemName"), "itemName", BigDecimal.apply(10), 1), 1, System.currentTimeMillis())
    case "checkout" =>
      cartManager ! StartCheckout
      context become inCheckout
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become receive
    case CheckoutStarted(c, _) =>
      val checkout = c
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected(System.currentTimeMillis())
      context become inPayment
    case CartEmpty => System.out.println("car has been emptied! kewl")
  }

  def inPayment(): Receive = LoggingReceive {
    case PaymentServiceStarted(service) =>
      service ! DoPayment
    case PaymentConfirmed =>
      context become inCheckout
  }
}
