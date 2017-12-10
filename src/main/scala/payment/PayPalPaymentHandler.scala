package payment

import akka.event.LoggingReceive
import payment.PaymentErrors.InvalidPaymentData
import payment.PaymentServer.PayPalPaymentData

class PayPalPaymentHandler extends PaymentHandlingActor[PayPalPaymentData] {
  override def receive: Receive = LoggingReceive {
    case data: PayPalPaymentData =>
      if (verifyData(data)) sender ! "success"
      else sender ! InvalidPaymentData
      context.stop(self)
  }

  override def verifyData(data: PayPalPaymentData): Boolean = {
    data.login != data.password && data.login != "" && data.password != ""
  }
}
