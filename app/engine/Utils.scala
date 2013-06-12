package engine

import java.util.Date
import java.text.SimpleDateFormat
import play.api.Logger
import org.joda.time.{PeriodType, DateTime, Instant, Interval}

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

  def daysLeftFromDate(value: Option[Date]) : String = {
    value match {
      case Some(value) => {
        val interval = new Interval(new Instant(), new DateTime(value))
        val days = interval.toPeriod(PeriodType.days()).getDays
        if (days < 60) {
          var t = "days"
          if (days == 1) t = "day"
          s"Ends in $days $t"
        }
        else {
          val dformat = new SimpleDateFormat("MMM d, yyyy")
          val dt = dformat.format(value)
          s"Ends $dt"
        }
      }
      case None => ""
    }
  }

  def toInt(value: String, default: Int) : Int =  {
    try {
      value.toInt
    } catch {
      case e:Exception => default
    }
  }

}
