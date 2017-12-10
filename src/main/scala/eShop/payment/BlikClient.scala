package eShop.payment

import akka.http.scaladsl.model._
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global

class BlikClient(code: String) extends PaymentClientActor {
  System.out.println("Blik actor created.")

  override def preStart(): Unit = {
    val formData = FormData(Map("code" -> code))
    http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/blik", entity = formData.toEntity))
      .pipeTo(self)
  }
}
