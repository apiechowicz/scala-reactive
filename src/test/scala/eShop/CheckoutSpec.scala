package eShop

import akka.actor.{ActorSystem, IllegalActorStateException, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CheckoutSpec extends TestKit(ActorSystem("CheckoutSpec")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  import CartManager._
  import Checkout._
  import PaymentService._

  var cart: TestProbe = _
  var checkout: TestActorRef[Checkout] = _

  override protected def beforeAll(): Unit = {
    cart = TestProbe("CheckoutSpec")
  }

  override def afterAll(): Unit = {
    system.terminate
  }

  "Checkout" must {
    "inform cart when checkout is closed" in {
      checkout = TestActorRef(Props[Checkout], cart.testActor)
      checkout ! CheckoutStarted(null)
      assert(checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected
      expectMsgPF() {
        case PaymentServiceStarted(_) => ()
      }
      assert(!checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
      assert(checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.paymentTimer))
      checkout ! PaymentReceived
      // assertion below will fail if actor was shutdown (which happens from time to time)
      try {
        assert(!checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.paymentTimer))
      } catch {
        case _: IllegalActorStateException => ()
      }
      cart.expectMsg(CheckoutClosed)
    }
  }

  "Checkout" can {
    "be cancelled when selecting delivery method" in {
      checkout = TestActorRef(Props[Checkout], cart.testActor)
      checkout ! CheckoutStarted(null)
      assert(checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
      checkout ! CheckoutCancelled
      assert(!checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
    }

    "be cancelled when selecting payment method" in {
      checkout = TestActorRef(Props[Checkout], cart.testActor)
      checkout ! CheckoutStarted(null)
      assert(checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
      checkout ! DeliveryMethodSelected
      checkout ! PaymentSelected
      expectMsgPF() {
        case PaymentServiceStarted(_) => ()
      }
      assert(!checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.checkoutTimer))
      assert(checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.paymentTimer))
      checkout ! CheckoutCancelled
      assert(!checkout.underlyingActor.timers.isTimerActive(checkout.underlyingActor.paymentTimer))
    }
  }
}
