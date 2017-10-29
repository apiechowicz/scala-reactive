package eShop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CheckoutSpec extends TestKit(ActorSystem("CheckoutSpec")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  import Cart._
  import Checkout._
  import PaymentService._

  var cart: TestProbe = _
  var checkout: ActorRef = _

  override protected def beforeAll(): Unit = {
    cart = TestProbe("CheckoutSpec")

    checkout = cart.childActorOf(Props[Checkout], "Checkout")
  }

  override def afterAll(): Unit = {
    system.terminate
  }

  "Checkout" must {
    "inform cart when checkout is closed" in {
      checkout ! CheckoutStarted(null)
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected
      checkout ! PaymentReceived
      cart.expectMsg(CheckoutClosed)
    }
  }
}
