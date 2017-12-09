package catalog

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.LoggingReceive
import catalog.ProductStoreManager.FindProducts
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProductCatalog extends App {
  private val catalog = "catalog"
  private val config = ConfigFactory.load()
  private val dbFile = config.getString(catalog + ".source-file")

  private val system: ActorSystem = ActorSystem("ProductCatalog", config.getConfig(catalog))
  private val productCatalog = system.actorOf(ProductCatalog.props(dbFile), catalog)
  Await.result(system.whenTerminated, Duration.Inf)

  def props(file: String): Props = Props(new ProductCatalog(file))
}

class ProductCatalog(file: String) extends Actor {

  private val productStore = context.actorOf(ProductStoreManager.props(file), "productStore")

  override def receive: Receive = LoggingReceive {
    case FindProducts(query) => productStore.forward(FindProducts(query))
  }
}
