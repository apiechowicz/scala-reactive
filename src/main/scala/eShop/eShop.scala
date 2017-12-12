package eShop

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object eShop extends App {
  override def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val system = ActorSystem("eShop", config.getConfig("eShop").withFallback(config))
    val customer = system.actorOf(Props[Customer], "Customer")

    /*    customer ! "add"
        customer ! "add"
        customer ! "remove"*/

    for (i <- 1 to 10) {
      System.out.println("\tEnter query:")
      val query = scala.io.StdIn.readLine()
      customer ! query
    }

    /*    Thread.sleep(5 * 1000)
        system.terminate()
        Thread.sleep(5 * 1000)

        val system2 = ActorSystem("eShop", config.getConfig("eShop").withFallback(config))
        val customer2 = system2.actorOf(Props[Customer], "Customer")
        customer2 ! "checkout"*/
  }
}
