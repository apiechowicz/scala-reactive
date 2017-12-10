package eShop.payment

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import eShop.PaymentService.PaymentSucceeded

trait PaymentClientActor extends Actor with ActorLogging {

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive: Receive = LoggingReceive {
    case HttpResponse(StatusCodes.OK, _, _, _) =>
      log.info(StatusCodes.OK.toString())
      context.parent ! PaymentSucceeded
    case HttpResponse(error: StatusCodes.ClientError, _, _, _) =>
      log.info(error.toString())
      throw new IllegalRequestException(ErrorInfo(error.defaultMessage, error.reason), error)
    case HttpResponse(error: StatusCodes.ServerError, _, _, _) =>
      log.info(error.toString())
      throw new IllegalResponseException(ErrorInfo(error.defaultMessage, error.reason))
    case Failure(exception) => throw exception
  }
}
