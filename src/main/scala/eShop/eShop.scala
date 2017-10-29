package eShop

import akka.actor.{ActorSystem, PoisonPill, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object eShop extends App {
  val system = ActorSystem("eShop")

  //      testCartAddRemove()
  //      testCartAddAddRemove()
  //      testCartAddCheckoutTimerExpireClose()
  //      testCartAddTimerExpire()

  //  testCheckoutStartExpire()
  //  testCheckoutStartDeliverySelectCancel()
  //  testCheckoutStartDeliverySelectPaymentSelectExpire()
  //  testCheckoutStartDeliverySelectPaymentSelectExpireReceive()

  Await.result(system.whenTerminated, Duration.Inf)

  def testCheckoutStartExpire(): Unit = {
    import Cart._
    System.out.println("---------- start & expire ----------")
    val checkout = system.actorOf(Props[Checkout], "CheckoutStartExpire")
    checkout ! CheckoutStarted
    Thread.sleep(31 * 1000)
    checkout ! PoisonPill
  }

  def testCheckoutStartDeliverySelectCancel(): Unit = {
    import Cart._
    import Checkout._
    System.out.println("---------- start & select delivery & cancel ----------")
    val checkout = system.actorOf(Props[Checkout], "CheckoutStartDeliverySelectCancel")
    checkout ! CheckoutStarted
    checkout ! DeliveryMethodSelected
    checkout ! CheckoutCancelled
    checkout ! PoisonPill
  }

  def testCheckoutStartDeliverySelectPaymentSelectExpire(): Unit = {
    import Cart._
    import Checkout._
    System.out.println("---------- start & select delivery & select payment & timeout ----------")
    val checkout = system.actorOf(Props[Checkout], "CheckoutStartDeliverySelectPaymentSelectExpire")
    checkout ! CheckoutStarted
    checkout ! DeliveryMethodSelected
    checkout ! PaymentSelected
    Thread.sleep(31 * 1000)
    checkout ! PoisonPill
  }

  def testCheckoutStartDeliverySelectPaymentSelectExpireReceive(): Unit = {
    import Cart._
    import Checkout._
    System.out.println("---------- start & select delivery & select payment & receive ----------")
    val checkout = system.actorOf(Props[Checkout], "CheckoutStartDeliverySelectPaymentSelectExpireReceive")
    checkout ! CheckoutStarted
    checkout ! DeliveryMethodSelected
    checkout ! PaymentSelected
    checkout ! PaymentReceived
    checkout ! PoisonPill
  }

  private def testCartAddRemove(): Unit = {
    import Cart._
    System.out.println("---------- add & remove ----------")
    val cart = system.actorOf(Props[Cart], "CartAddRemove")
    cart ! ItemAdded
    cart ! ItemRemoved
    cart ! PoisonPill
  }

  private def testCartAddAddRemove(): Unit = {
    import Cart._
    System.out.println("---------- add & add & remove ----------")
    val cart = system.actorOf(Props[Cart], "CartAddAddRemove")
    cart ! ItemAdded
    cart ! ItemAdded
    cart ! ItemRemoved
    cart ! PoisonPill
  }

  private def testCartAddCheckoutTimerExpireClose(): Unit = {
    import Cart._
    System.out.println("---------- add & checkout & timer expire & close checkout ----------")
    val cart = system.actorOf(Props[Cart], "CartAddCheckoutWaitClose")
    cart ! ItemAdded
    cart ! CheckoutStarted
    Thread.sleep(31 * 1000)
    cart ! CheckoutClosed
    cart ! PoisonPill
  }

  private def testCartAddTimerExpire(): Unit = {
    import Cart._
    System.out.println("---------- add & timer expire ----------")
    val cart = system.actorOf(Props[Cart], "CartAddExpire")
    cart ! ItemAdded
    Thread.sleep(31 * 1000)
    cart ! PoisonPill
  }
}
