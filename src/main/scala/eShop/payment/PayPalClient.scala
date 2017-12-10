package eShop.payment

import akka.http.scaladsl.model._
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global

class PayPalClient(login: String, password: String) extends PaymentClientActor {
  override def preStart(): Unit = {
    System.out.println("PayPal actor created.")
    val formData = FormData(Map("login" -> login, "password" -> password))
    http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/paypal", entity = formData.toEntity))
      .pipeTo(self)
  }
}
