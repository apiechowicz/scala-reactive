package payment

import akka.actor.Actor
import akka.http.scaladsl.model.StatusCode

import scala.util.Random

trait PaymentHandlingActor[T] extends Actor {
  val random: Random.type = Random

  def verifyData(data: T): Boolean

  def isNumberOfLength(number: String, length: Int): Boolean = {
    try {
      number.toInt
    } catch {
      case nfe: NumberFormatException => return false
    }
    number.length == length
  }

  def shouldFail(): Boolean = {
    random.nextInt(100) > 49
  }

  def returnRandomError(): StatusCode = {
    if (random.nextInt(100) > 49) {
      PaymentErrors.clientErrors(random.nextInt(PaymentErrors.clientErrors.size))
    } else {
      PaymentErrors.serverErrors(random.nextInt(PaymentErrors.serverErrors.size))
    }
  }
}
