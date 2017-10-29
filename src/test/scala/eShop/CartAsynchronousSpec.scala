package eShop

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartAsynchronousSpec extends TestKit(ActorSystem("CartAsynchronousSpec")) with ImplicitSender with WordSpecLike
  with BeforeAndAfterAll {

  var asynchronousCart: ActorRef = _

  import Cart._
  import Customer._

  override protected def beforeAll(): Unit = {
    asynchronousCart = system.actorOf(Props[Cart], "CartActor")
    asynchronousCart ! ItemAdded
  }

  override def afterAll(): Unit = {
    system.terminate
  }

  "A Cart (asynchronous)" must {
    "become Empty after removing item" in {
      asynchronousCart ! ItemRemoved
      expectMsg(CartEmpty)
    }

    "start checkout" in {
      asynchronousCart ! ItemAdded
      asynchronousCart ! StartCheckout
      expectMsgPF() {
        case CheckoutStarted(_) => ()
      }
    }

    // wont work because message will be delivered to parent
/*
    "become empty after checkout was closed" in {
      asynchronousCart ! CheckoutClosed
      expectMsg(CartEmpty)
    }
*/
  }
}
