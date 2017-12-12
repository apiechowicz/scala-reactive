package payment

import akka.event.LoggingReceive
import akka.http.scaladsl.model.StatusCodes
import payment.PaymentServer.CreditCardPaymentData

class CreditCardPaymentHandler extends PaymentHandlingActor[CreditCardPaymentData] {
  override def receive: Receive = LoggingReceive {
    case data: CreditCardPaymentData =>
      if (verifyData(data)) sender ! StatusCodes.OK
      else sender ! StatusCodes.BadRequest
      context.stop(self)
  }

  override def verifyData(data: CreditCardPaymentData): Boolean = {
    isCardNumberValid(data.cardNumber) && isExpirationDateValid(data.expirationDate) && isOwnerValid(data.owner) && isCvvValid(data.cvv)
  }

  private def isCardNumberValid(cardNumber: String): Boolean = {
    var allNumbers = true
    cardNumber.foreach(c => allNumbers = allNumbers && isNumberOfLength(c.toString, 1))
    allNumbers
  }

  private def isExpirationDateValid(expirationDate: String): Boolean = {
    val date = expirationDate.split("/")
    if (date.length != 2) return false
    try {
      val monthValid = valueInRange(date(0).toInt, 1, 12)
      val yearValid = valueInRange(date(1).toInt, 0, 99)
      monthValid && yearValid
    } catch {
      case nfe: NumberFormatException => false
    }
  }

  private def valueInRange(value: Int, lowerBound: Int, upperBound: Int): Boolean = {
    value match {
      case x if lowerBound to upperBound contains x => true
      case _ => false
    }
  }

  private def isOwnerValid(owner: String): Boolean = {
    val name = owner.split(" ")
    name.length == 2 && name(0) != "" && name(1) != ""
  }

  private def isCvvValid(cvv: String): Boolean = {
    isNumberOfLength(cvv, 3)
  }
}
