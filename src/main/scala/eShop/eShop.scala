package eShop

import akka.actor.{ActorSystem, Props}

object eShop extends App {
  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("eShop")
    val customer = system.actorOf(Props[Customer], "Customer")
    customer ! "add"
    customer ! "add"
    customer ! "remove"
    customer ! "checkout"
  }
}
