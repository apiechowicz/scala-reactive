package eShop

import java.util.concurrent.TimeUnit

import akka.actor.FSM

import scala.concurrent.duration.FiniteDuration

// events
final case class CheckoutTimerExpired()

final case class DeliveryMethodSelected()

final case class PaymentSelected()

final case class PaymentReceived()

final case class PaymentTimerExpired()

// states
sealed trait CheckoutState

case object SelectingDelivery extends CheckoutState

case object SelectingPaymentMethod extends CheckoutState

case object ProcessingPayment extends CheckoutState

case object Closed extends CheckoutState

case object Cancelled extends CheckoutState

sealed trait CheckoutData

final case class NoData() extends CheckoutData

class Checkout extends FSM[CheckoutState, CheckoutData] {

  val checkoutTimerName = "CheckoutTimerFSM"
  val checkoutTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  val paymentTimerName = "PaymentTimerFSM"
  val paymentTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  startWith(SelectingDelivery, NoData())

  when(SelectingDelivery) {
    case Event(DeliveryMethodSelected, _) => goto(SelectingPaymentMethod)
  }
  when(SelectingPaymentMethod) {
    case Event(PaymentSelected, _) => goto(SelectingPaymentMethod)
  }

  when(ProcessingPayment) {
    case Event(PaymentReceived, _) => goto(Closed)
    case Event(PaymentTimerExpired, _) => goto(Cancelled)
  }

  when(Closed) {
    ???
  }

  when(Cancelled) {
    ???
  }

  onTransition {
    case _ -> SelectingDelivery => setTimer(checkoutTimerName, CartTimerExpired, checkoutTimeout, repeat = false)
    case SelectingDelivery -> SelectingPaymentMethod => setTimer(checkoutTimerName, CartTimerExpired, checkoutTimeout, repeat = false)
    case SelectingPaymentMethod -> ProcessingPayment =>
      cancelTimer(checkoutTimerName)
      setTimer(paymentTimerName, CartTimerExpired, paymentTimeout, repeat = false)
    case ProcessingPayment -> Closed => cancelTimer(paymentTimerName)
    case _ -> Cancelled =>
      cancelTimer(checkoutTimerName)
      cancelTimer(paymentTimerName)
  }

  whenUnhandled {
    case Event(CheckoutCancelled, _) =>
      if (stateName != Closed && stateName != Cancelled) goto(Cancelled)
      else stay
    case Event(CheckoutTimerExpired, _) =>
      if (stateName == SelectingDelivery || stateName == SelectingPaymentMethod) goto(Cancelled)
      else stay
  }

  initialize()
}
