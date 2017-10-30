package eShop

import akka.actor.Actor
import akka.event.LoggingReceive

object PaymentService {

  case class PaymentConfirmed()

  case class PaymentReceived()

}

class PaymentService extends Actor {

  import Customer.DoPayment
  import PaymentService._

  override def receive: Receive = LoggingReceive {
    case DoPayment =>
      if (isPaymentVerified) {
        sender ! PaymentConfirmed
        context.parent ! PaymentReceived
      }
  }

  private def isPaymentVerified: Boolean = {
    Thread.sleep(5000)
    true
  }
}
