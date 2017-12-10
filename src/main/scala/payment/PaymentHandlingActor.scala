package payment

import akka.actor.Actor

trait PaymentHandlingActor[T] extends Actor {
  def verifyData(data: T): Boolean

  def isNumberOfLength(number: String, length: Int): Boolean = {
    try {
      number.toInt
    } catch {
      case nfe: NumberFormatException => return false
    }
    number.length == length
  }
}
