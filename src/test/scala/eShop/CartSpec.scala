package eShop

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartSpec extends TestKit(ActorSystem("CartSynchronousSpec")) with WordSpecLike with BeforeAndAfterAll {

  import Cart._

  var synchronousCart: TestActorRef[Cart] = _

  override protected def beforeAll(): Unit = {
    synchronousCart = TestActorRef[Cart]
  }

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart (synchronous)" must {
    "start as empty" in {
      assert(synchronousCart.underlyingActor.itemCount == 0)
      assert(!synchronousCart.underlyingActor.timers.isTimerActive(synchronousCart.underlyingActor.cartTimer))
    }

    "change state to NonEmpty after adding item to Empty Cart" in {
      assert(synchronousCart.underlyingActor.itemCount == 0)
      synchronousCart ! ItemAdded
      assert(synchronousCart.underlyingActor.itemCount == 1)
    }

    "become Empty again after removing item from Cart" in {
      assert(synchronousCart.underlyingActor.itemCount == 1)
      synchronousCart ! ItemRemoved
      assert(synchronousCart.underlyingActor.itemCount == 0)
    }
  }
}
