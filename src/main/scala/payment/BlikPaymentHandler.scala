package payment

import akka.event.LoggingReceive
import payment.PaymentErrors.InvalidPaymentData
import payment.PaymentServer.BlikPaymentData

class BlikPaymentHandler extends PaymentHandlingActor[BlikPaymentData] {
  override def receive: Receive = LoggingReceive {
    case data: BlikPaymentData =>
      if (verifyData(data)) sender ! "success"
      else sender ! InvalidPaymentData
      context.stop(self)
  }

  override def verifyData(data: BlikPaymentData): Boolean = {
    isNumberOfLength(data.code, 6)
  }
}
