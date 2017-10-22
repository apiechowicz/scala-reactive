package eShop

import java.util.concurrent.TimeUnit

import akka.actor.FSM

import scala.concurrent.duration.FiniteDuration

// events
final case class ItemAdded()

final case class ItemRemoved()

final case class CartTimerExpired()

final case class CheckoutStarted()

final case class CheckoutCancelled()

final case class CheckoutClosed()

// states
sealed trait CartState

case object Empty extends CartState

case object NonEmpty extends CartState

case object InCheckout extends CartState

sealed trait CartData

final case class ItemCount(value: Int) extends CartData

class Cart extends FSM[CartState, CartData] {
  val timerName = "CartTimerFSM"
  val timerTimeout = FiniteDuration(30, TimeUnit.SECONDS)

  startWith(Empty, ItemCount(0))

  when(Empty) {
    case Event(ItemAdded, _) => goto(NonEmpty) using ItemCount(1)
  }

  when(NonEmpty) {
    case Event(ItemAdded, _) => stay using ItemCount(stateData.value + 1)
    case Event(ItemRemoved, _) =>
      if (stateData.value > 1) {
        stay using ItemCount(stateData.value - 1)
      } else {
        goto(Empty) using ItemCount(0)
      }
    case Event(CheckoutStarted, _) => goto(InCheckout) using stateData
  }

  when(InCheckout) {
    case Event(CheckoutClosed, _) => goto(Empty) using ItemCount(0)
    case Event(CheckoutCancelled, _) => goto(NonEmpty) using stateData
  }

  onTransition {
    case _ -> NonEmpty => setTimer(timerName, CartTimerExpired, timerTimeout, repeat = false)
    case NonEmpty -> _ => cancelTimer(timerName)
  }

  whenUnhandled {
    case Event(CartTimerExpired, _) =>
      if (stateName == NonEmpty) {
        goto(Empty) using ItemCount(0)
      } else {
        stay using stateData
      }
  }

  initialize()
}
