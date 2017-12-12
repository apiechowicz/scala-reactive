package eShop.payment

import akka.http.scaladsl.model._
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global

class CreditCardClient(cardNumber: String, expirationDate: String, owner: String, cvv: String) extends PaymentClientActor {
  override def preStart(): Unit = {
    System.out.println("CreditCard actor created.")
    val formData = FormData(Map("cardNumber" -> cardNumber, "expirationDate" -> expirationDate, "owner" -> owner, "cvv" -> cvv))
    http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/credit-card", entity = formData.toEntity))
      .pipeTo(self)
  }
}
