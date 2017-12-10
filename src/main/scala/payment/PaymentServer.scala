package payment

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import payment.PaymentServer.{BlikPaymentData, CreditCardPaymentData, PayPalPaymentData}

import scala.concurrent.duration.FiniteDuration
import scala.util.Success

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
    } ~
      pathPrefix("blik") {
        pathEndOrSingleSlash {
          post {
            formField('code).as(BlikPaymentData) {
              paymentData =>
                val blikHandler = system.actorOf(Props[BlikPaymentHandler])
                onComplete(blikHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      } ~
      pathPrefix("credit-card") {
        pathEndOrSingleSlash {
          post {
            formField('cardNumber, 'expirationDate, 'owner, 'cvv).as(CreditCardPaymentData) {
              paymentData =>
                val cardHandler = system.actorOf(Props[CreditCardPaymentHandler])
                onComplete(cardHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      } ~
      pathPrefix("paypal") {
        pathEndOrSingleSlash {
          post {
            formField('login, 'password).as(PayPalPaymentData) {
              paymentData =>
                val paypalHandler = system.actorOf(Props[PayPalPaymentHandler])
                onComplete(paypalHandler ? paymentData) {
                  case Success(status: StatusCode) => complete(status)
                  case _ => complete(StatusCodes.InternalServerError)
                }
            }
          }
        }
      }
  }
}
