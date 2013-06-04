package engine

import java.util.Date
import java.text.SimpleDateFormat
import play.api.Logger

object Utils {

  val dateFormatter = new SimpleDateFormat("MM/dd/yyyy")

  def dateFromString(value: Option[String]) : Option[Date] = {
    value match {
      case Some(value) => {
        try {
          Some(dateFormatter.parse(value))
        } catch {
          case e: Exception => {
            Logger.error("Error parsing date" + e.getMessage)
            None
          }
        }
      }
      case None => None
    }
  }

}
