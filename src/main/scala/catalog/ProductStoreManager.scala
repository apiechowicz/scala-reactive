package catalog

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive
import catalog.ProductStoreManager.FindProducts

object ProductStoreManager {

  def props(file: String): Props = Props(new ProductStoreManager(new ProductStore(file)))

  case class FindProducts(query: String)

}

class ProductStoreManager(productStore: ProductStore) extends Actor {
  override def receive: Receive = LoggingReceive {
    case FindProducts(query) => sender ! productStore.getBestMatch(query)
  }
}
