package payment

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration

object PaymentServer extends App {

  private val interface = "localhost"

  private val port = 8080

  private val server = new PaymentServer()

  server.startServer(interface, port)

  final case class BlikPaymentData(code: String)

  final case class CreditCardPaymentData(cardNumber: String, expirationDate: String, owner: String, cvv: String)

  final case class PayPalPaymentData(login: String, password: String)

}

class PaymentServer extends HttpApp {

  private val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("PaymentServer", config.getConfig("payment").withFallback(config))

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  override protected def routes: Route = {
    pathEndOrSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>PaymentServer welcome!</h1>"))
      }
    }
    /*    pathPrefix("blik") {
          pathEndOrSingleSlash {
            post {

            }
          }
        }
        pathPrefix("credit-card") {
          pathEndOrSingleSlash {
            post {

            }
          }
        }
        pathPrefix("paypal") {
          pathEndOrSingleSlash {
            post {

            }
          }
        }*/
  }
}
