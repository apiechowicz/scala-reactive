package catalog

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.event.LoggingReceive
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import catalog.ProductStoreManager.FindProducts
import com.typesafe.config.ConfigFactory
import eShop.Item

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProductCatalog extends App {
  private val catalog = "catalog"
  private val config = ConfigFactory.load()
  private val dbFile = config.getString(catalog + ".source-file")

  private val systemName = "ProductCatalog"
  implicit val system: ActorSystem = ActorSystem(systemName, config.getConfig(catalog).withFallback(config))

  private val productCatalog = system.actorOf(ProductCatalog.props(dbFile), catalog)

  RestProductCatalog.startServer(productCatalog)

  Await.result(system.whenTerminated, Duration.Inf)

  def props(file: String): Props = Props(new ProductCatalog(file))
}

class ProductCatalog(file: String) extends Actor {

  private val numberOfWorkers = 10

  private var items: List[Item] = _

  private var router: Router = _

  override def preStart(): Unit = {
    items = ProductParser.parseProducts(file)
    router = Router(RoundRobinRoutingLogic(), createWorkers)
  }

  private def createWorkers = {
    Vector.fill(numberOfWorkers) {
      val r = context.actorOf(ProductStoreManager.props(items))
      context watch r
      ActorRefRoutee(r)
    }
  }

  override def receive: Receive = LoggingReceive {
    case FindProducts(query) => router.route(FindProducts(query), sender)
    case Terminated(worker) ⇒ replaceTerminatedWorker(worker)
  }

  private def replaceTerminatedWorker(worker: ActorRef): Unit = {
    router = router.removeRoutee(worker)
    val r = context.actorOf(ProductStoreManager.props(items))
    context watch r
    router = router.addRoutee(r)
  }
}
