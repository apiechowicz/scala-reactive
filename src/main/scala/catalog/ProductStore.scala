package catalog

import eShop.Item

class ProductStore(file: String) {

  private val items = ProductParser.parseProducts(file)

  private val numberOfBestMatches = 10

  private[catalog] def getBestMatch(query: String): List[Item] = {
    val keywords = query.split(" ")
    items.map(i => (i, countKeywords(i, keywords)))
      .sortBy(_._2)
      .reverse
      .take(numberOfBestMatches)
      .map(i => i._1)
  }

  private def countKeywords(item: Item, keywords: Array[String]): Int = {
    item.name.split(" ")
      .map(s => if (keywords.contains(s)) 1 else 0)
      .sum
  }
}
