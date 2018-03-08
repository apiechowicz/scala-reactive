package catalog

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive
import catalog.ProductStoreManager.FindProducts
import eShop.Item

object ProductStoreManager {

  def props(items: List[Item]): Props = Props(new ProductStoreManager(new ProductStore(items)))

  case class FindProducts(query: String)

}

class ProductStoreManager(productStore: ProductStore) extends Actor {
  override def receive: Receive = LoggingReceive {
    case FindProducts(query) =>
      System.out.println("Actor %s is handling query.".format(this.toString))
      sender ! productStore.getBestMatch(query)
  }
}
