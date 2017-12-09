package catalog

import java.net.URI

import eShop.Item

object ProductParser {

  private val itemPrice = 20

  private val itemCount = 15

  private[catalog] def parseProducts(file: String): List[Item] = {
    scala.io.Source.fromFile(file).getLines()
      .map(_.replace("\"", ""))
      .map(parseProduct)
      .toList
  }

  private def parseProduct(line: String): Item = {
    val productInfo = line.split(",")
    val uri = URI.create(productInfo(0).trim)
    val name = parseName(productInfo)
    Item(uri, name, itemPrice, itemCount)
  }

  private def parseName(productInfo: Array[String]): String = {
    val name = new StringBuilder(productInfo(1).trim)
    if (productInfo.length >= 3 && productInfo(2) != "NULL")
      name.append(" " + productInfo(2).trim)
    name.toString()
  }
}
