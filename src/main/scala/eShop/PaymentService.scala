package eShop

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import akka.event.LoggingReceive
import akka.http.scaladsl.model.{IllegalRequestException, IllegalResponseException}
import akka.stream.StreamTcpException

object PaymentService {

  case class PaymentConfirmed()

  case class PaymentReceived()

  case class PaymentSucceeded()

}

class PaymentService extends Actor {

  import Customer._
  import PaymentService._
  import payment._

  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = -1) {
    case _: IllegalRequestException => Restart
    case _: IllegalResponseException => Restart
    case _: StreamTcpException => Restart
    case _: Exception => Resume
  }
  private var customer: ActorRef = _

  override def receive: Receive = LoggingReceive {
    case DoPayment(method: PaymentMethod) =>
      customer = sender
      method match {
        case Blik(code) => context.actorOf(Props(classOf[BlikClient], code), "Blik")
        case CreditCard(cardNumber, expirationDate, owner, cvv) => context.actorOf(Props(classOf[CreditCardClient], cardNumber, expirationDate, owner, cvv), "CreditCard")
        case PayPal(login, password) => context.actorOf(Props(classOf[PayPalClient], login, password), "PayPal")
      }
    case PaymentSucceeded =>
      customer ! PaymentConfirmed
      context.parent ! PaymentReceived
  }
}
