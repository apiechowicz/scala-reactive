package catalog

import java.util.concurrent.TimeUnit

import _root_.catalog.ProductStoreManager.FindProducts
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import eShop.Item
import org.json4s._
import org.json4s.jackson.Serialization.write

import scala.concurrent.duration.FiniteDuration
import scala.util.Success

object RestProductCatalog {

  private val interface = "localhost"

  private val port = 9001

  def startServer(productCatalog: ActorRef): Unit = {
    val rest = new RestProductCatalog(productCatalog)
    rest.startServer(interface, port)
  }
}

class RestProductCatalog(productCatalog: ActorRef) extends HttpApp {

  private val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("Rest", config.getConfig("rest").withFallback(config))

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  implicit val formats: DefaultFormats.type = DefaultFormats

  override protected def routes: Route = {
    pathPrefix("catalog") {
      pathEndOrSingleSlash {
        post {
          formField('query).as(FindProducts) {
            query =>
              onComplete(productCatalog ? query) {
                case Success(items: List[Item]) =>
                  val json = write(items)
                  complete(json)
                case _ => complete(StatusCodes.InternalServerError)
              }
          }
        }
      }
    }
  }
}
