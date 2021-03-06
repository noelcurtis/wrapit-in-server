package engine

import play.Logger
import java.net.URL
import org.jsoup.Jsoup
import scala.collection.mutable

object ImageGetter {

  def getImages(url: Option[String]): List[(String, Int)] = {
    url match {
      case Some(url) => getImages(url)
      case None => List()
    }
  }


  def getImages(url: String): List[(String, Int)] = {
    Logger.info(s"Getting images {$url} started")
    try {
      val doc = Jsoup.connect(url).get() // GET request to the URL
      val images = doc.select("img")
      val iterator = images.iterator()
      var srcs: mutable.ListBuffer[(String, Int)] = mutable.ListBuffer()
      while (iterator.hasNext) {
        val cur = iterator.next()
        val attrs = (cur.attr("src"), Utils.toInt(cur.attr("width"), 0))
        srcs += attrs
      }
      val found = srcs.toList sortBy (-_._2)
      val filtered = found.filter(x => validateUrl(x._1)) // TODO: Decide whether its worth making a connection to the URL
      Logger.info(s"Getting images {$url} ended")
      filtered
    } catch {
      case e:Exception => Logger.error("Error getting images " + e.getMessage); List()
    }
  }

  def validateUrl(testUrl: String, withConnection: Boolean): Boolean = {
    var valid = true
    try {
      val url = new URL(testUrl)
      if (withConnection) {
        val conn = url.openConnection()
        conn.connect();
      }
    } catch {
      case e: Exception => Logger.info("Invalid URL: " + e.getMessage); valid = false
    }
    valid
  }

  // Does not work for all URL's TODO: change URL validation to use WEB requests with WS instead.
  def validateUrl(testUrl: String): Boolean = {
       validateUrl(testUrl, false);
  }

}
