package eShop

import akka.actor.{ActorSystem, Props}

object eShop extends App {
  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("eShop")
    val customer = system.actorOf(Props[Customer], "Customer")

    customer ! "add"
    customer ! "add"
    customer ! "remove"

    System.out.println("\tEnter query:")
    val query = scala.io.StdIn.readLine()
    customer ! query

    Thread.sleep(5 * 1000)
    system.terminate()
    Thread.sleep(5 * 1000)

    val system2 = ActorSystem("eShop")
    val customer2 = system2.actorOf(Props[Customer], "Customer")
    customer2 ! "checkout"
  }
}
