package eShop

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartManagerSpec extends TestKit(ActorSystem("CartSpec")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  import CartManager._
  import Customer._

  var cart: TestActorRef[CartManager] = _

  override protected def beforeAll(): Unit = {
    cart = TestActorRef[CartManager]
  }

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart" must {
    "start as empty" in {
      assert(cart.underlyingActor.itemCount == 0)
      assert(!cart.underlyingActor.timers.isTimerActive(cart.underlyingActor.cartTimer))
    }

    "change state to NonEmpty after adding item to Empty Cart" in {
      assert(cart.underlyingActor.itemCount == 0)
      cart ! ItemAdded
      assert(cart.underlyingActor.itemCount == 1)
      assert(cart.underlyingActor.timers.isTimerActive(cart.underlyingActor.cartTimer))
    }

    "become Empty again after removing item from Cart" in {
      assert(cart.underlyingActor.itemCount == 1)
      cart ! ItemRemoved
      expectMsg(CartEmpty)
      assert(cart.underlyingActor.itemCount == 0)
      assert(!cart.underlyingActor.timers.isTimerActive(cart.underlyingActor.cartTimer))
    }

    "start checkout" in {
      cart ! ItemAdded
      cart ! StartCheckout
      expectMsgPF() {
        case CheckoutStarted(_) => ()
      }
      assert(cart.underlyingActor.itemCount != 0)
      assert(!cart.underlyingActor.timers.isTimerActive(cart.underlyingActor.cartTimer))
    }
  }
}
