package payment

import akka.event.LoggingReceive
import akka.http.scaladsl.model.StatusCodes
import payment.PaymentServer.PayPalPaymentData

class PayPalPaymentHandler extends PaymentHandlingActor[PayPalPaymentData] {
  override def receive: Receive = LoggingReceive {
    case data: PayPalPaymentData =>
      if (verifyData(data)) sender ! StatusCodes.OK
      else sender ! StatusCodes.BadRequest
      context.stop(self)
  }

  override def verifyData(data: PayPalPaymentData): Boolean = {
    data.login != data.password && data.login != "" && data.password != ""
  }
}
