package eShop

import java.net.URI

case class Item(id: URI, name: String, price: BigDecimal, count: Int)

case class Cart(items: Map[URI, Item]) {
  def addItem(item: Item): Cart = {
    val currentCount = if (items contains item.id) items(item.id).count else 0
    copy(items.updated(item.id, item.copy(count = currentCount + item.count)))
  }

  def removeItem(item: Item, count: Int): Cart = {
    val currentCount = if (items contains item.id) items(item.id).count else 0
    if (count <= 0 || currentCount == 0) return this
    if ((currentCount - count) > 0) {
      return copy(items.updated(item.id, item.copy(count = currentCount - item.count)))
    }
    copy(items - item.id)
  }

  def isEmpty: Boolean = items.isEmpty
}

object Cart {
  val empty = Cart(Map.empty)
}
