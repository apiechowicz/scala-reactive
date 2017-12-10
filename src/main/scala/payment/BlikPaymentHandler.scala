package payment

import akka.event.LoggingReceive
import akka.http.scaladsl.model.StatusCodes
import payment.PaymentServer.BlikPaymentData

class BlikPaymentHandler extends PaymentHandlingActor[BlikPaymentData] {
  override def receive: Receive = LoggingReceive {
    case data: BlikPaymentData =>
      if (verifyData(data)) sender ! StatusCodes.OK
      else sender ! StatusCodes.BadRequest
      context.stop(self)
  }

  override def verifyData(data: BlikPaymentData): Boolean = {
    isNumberOfLength(data.code, 6)
  }
}
