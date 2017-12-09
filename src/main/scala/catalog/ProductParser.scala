package catalog

import java.net.URI

import akka.actor.Actor
import catalog.ProductParser.ParseRequest
import eShop.Item

object ProductParser {

  case class ParseRequest(fileName: String)

}

class ProductParser extends Actor {

  private val itemPrice = 20

  private val itemCount = 15

  override def receive: Receive = {
    case ParseRequest(file) => sender ! parseProducts(file)
  }

  private def parseProducts(file: String): List[Item] = {
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
    if (productInfo(2) != "NULL")
      name.append(" " + productInfo(2).trim)
    name.toString()
  }
}
