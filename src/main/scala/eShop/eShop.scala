package eShop

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object eShop extends App {
  val system = ActorSystem("eShop")
  val cart = system.actorOf(Props[Cart], "Cart")

  import Cart.ItemAdded

  cart ! ItemAdded

  Await.result(system.whenTerminated, Duration.Inf)
}
