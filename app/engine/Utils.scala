package engine

import java.util.Date
import java.text.SimpleDateFormat
import play.api.Logger
import org.joda.time.{PeriodType, DateTime, Instant, Interval}
import play.api.cache.Cache
import play.api.Play.current
import scala.Predef._
import play.api.libs.json.{Writes, Json, JsValue}
import scala.collection.mutable.ListBuffer
import play.api.libs.json.JsObject
import java.lang.String
import scala.Some

abstract sealed case class ItemRelationType(value: Int)

object ItemRelationType {
  def fromInt(value: Int) : ItemRelationType = {
    if (value == 1) Creator
    else if (value == 2) Purchaser
    else Creator
  }
}

object Creator extends ItemRelationType(1)
object Purchaser extends ItemRelationType(2)

object Utils {

  val dateFormatter = new SimpleDateFormat("MM/dd/yyyy")

  def getAwsBucketPath = {
    getAwsBasePath + getAwsBucket
  }

  def getAwsBasePath = {
    val path: String = Cache.getOrElse[String]("aws.basePath") {
      play.Play.application().configuration().getString("aws.basePath")
    }
    path
  }

  def getAwsBucket = {
    val bucket: String = Cache.getOrElse[String]("aws.bucket") {
      if (play.Play.isProd) {
        play.Play.application().configuration().getString("aws.prod.bucket")
      }
      else {
        play.Play.application().configuration().getString("aws.dev.bucket")
      }
    }
    bucket
  }

  def dateFromString(value: Option[String]): Option[Date] = {
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

  def daysLeftFromDate(value: Option[Date]): String = {
    value match {
      case Some(value) => {
        val valueDateTime = new DateTime(value)
        if (valueDateTime.isBeforeNow) {
          "Ended"
        } else {
          val interval = new Interval(new Instant(), valueDateTime)
          val days = interval.toPeriod(PeriodType.days()).getDays
          if (days == 0) {
            "Due today"
          }
          else if (days < 60) {
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
      }
      case None => ""
    }
  }

  def toInt(value: String, default: Int): Int = {
    try {
      value.toInt
    } catch {
      case e: Exception => default
    }
  }

  implicit val mapWrites : Writes[Map[Long, List[String]]] = new Writes[Map[Long, List[String]]] {
    def writes(v: Map[Long, List[String]]): JsValue = {
      val values: ListBuffer[(String, JsValue)] = ListBuffer()
      v.keys.foreach( key => values += (key.toString -> Json.toJson(v.get(key).get)))
      JsObject(values.toSeq)
    }
  }

}
