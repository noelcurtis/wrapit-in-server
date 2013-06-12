package engine

import play.Logger
import play.api.libs.ws.WS
import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.mutable

object ImageGetter {

  def getImages (url: Option[String]) : List[(String, Int)] = {
    url match {
      case Some(url) => getImages(url)
      case None => List()
    }
  }


  def getImages (url: String) : List[(String, Int)] = {
    Logger.info(s"Getting images {$url} started");
    val doc = Jsoup.connect(url).get()
    val images = doc.select("img")
    val iterator  = images.iterator()
    var srcs: mutable.ListBuffer[(String, Int)] = mutable.ListBuffer()
    while (iterator.hasNext)
    {
      val cur = iterator.next()
      val attrs = (cur.attr("src"), Utils.toInt(cur.attr("width"), 0))
      srcs += attrs
    }
    val found = srcs.toList sortBy(- _._2)
    val filtered = found.filter( x => validateUrl(x._1))
    Logger.info(s"Getting images {$url} ended");
    filtered
  }


  def validateUrl (testUrl: String) : Boolean = {
    var valid = true
    try {
      val url = new URL(testUrl);
      val conn = url.openConnection();
      conn.connect();
    } catch {
      case e:Exception => Logger.info("Invalid URL: " + e.getMessage); valid = false
    }
    valid
  }

}
