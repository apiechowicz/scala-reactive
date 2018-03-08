package eShop

import java.net.URI

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import catalog.ProductStoreManager.FindProducts

object Customer {

  case class StartCheckout()

  sealed trait PaymentMethod

  case class DoPayment(method: PaymentMethod)

  case class Blik(code: String) extends PaymentMethod

  case class CreditCard(cardNumber: String, expirationDate: String, owner: String, cvv: String) extends PaymentMethod

  case class PayPal(login: String, password: String) extends PaymentMethod

}

class Customer extends Actor {

  import CartManager._
  import Checkout._
  import Customer._
  import PaymentService._

  val cartId: Long = 1
  val cartManager: ActorRef = context.actorOf(Props(new CartManager(cartId, Cart.empty)), "CartManager")

  override def receive: Receive = LoggingReceive {
    case "add" => cartManager ! ItemAdded(Item(URI.create("itemName"), "itemName", BigDecimal.apply(10), 1), System.currentTimeMillis())
    case "remove" => cartManager ! ItemRemoved(Item(URI.create("itemName"), "itemName", BigDecimal.apply(10), 1), 1, System.currentTimeMillis())
    case "checkout" =>
      cartManager ! StartCheckout
      context become inCheckout
    case query: String =>
      val productStore = context.actorSelection("akka.tcp://ProductCatalog@127.0.0.1:22553/user/catalog")
      productStore ! FindProducts(query)
    case items: List[Item] => items.foreach(i => {
      cartManager ! ItemAdded(i, System.currentTimeMillis())
    })
  }

  def inCheckout(): Receive = LoggingReceive {
    case CheckoutClosed => context become receive
    case CheckoutStarted(c, _) =>
      val checkout = c
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected(System.currentTimeMillis())
      context become inPayment
    case CartEmpty => System.out.println("cart has been emptied!")
  }

  def inPayment(): Receive = LoggingReceive {
    case PaymentServiceStarted(service) =>
      service ! DoPayment(Blik("123456"))
    // service ! DoPayment(CreditCard("1234567812345678", "07/95", "John Doe", "777"))
    // service ! DoPayment(PayPal("login", "password"))
    case PaymentConfirmed =>
      context become inCheckout
  }
}
