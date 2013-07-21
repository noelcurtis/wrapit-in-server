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
import com.google.common.hash.Hashing
import org.im4java.core.{IMOperation, ConvertCmd}
import java.io.File

object Utils {

  val dateFormatter = new SimpleDateFormat("MM/dd/yyyy") //Global date format

  /**
   * Uset to get base AWS path "https://s3.amazonaws.com/wi-dev"
   * @return
   */
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

  /**
   * Use to get a date from a String
   * @param value
   * @return
   */
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

  /**
   * Helper to get time left from a date
   * @param value
   * @return
   */
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

  /**
   * Use to convert a String to an Int
   * @param value
   * @param default
   * @return
   */
  def toInt(value: String, default: Int): Int = {
    try {
      value.toInt
    } catch {
      case e: Exception => default
    }
  }

  /**
   * Use to convert a Map[Long, List[String]] to JSValue
   */
  implicit val mapWrites : Writes[Map[Long, List[String]]] = new Writes[Map[Long, List[String]]] {
    def writes(v: Map[Long, List[String]]): JsValue = {
      val values: ListBuffer[(String, JsValue)] = ListBuffer()
      v.keys.foreach( key => values += (key.toString -> Json.toJson(v.get(key).get)))
      JsObject(values.toSeq)
    }
  }

  /**
   * Use to get a AWS file path example: items/fjdfkdkfj93j-2.jpg
   * @param data
   * @param folder
   * @param extension
   */
  def getAwsFilePath(data: Array[Byte], folder: AWSFolder, extension: String) : (String, String) = {
    val hash = Hashing.sha256().hashBytes(data).toString // create a hash code from the array
    val filename = hash + "." + extension
    (folder.value, filename)
  }

  /**
   * Use to get path for a Temp file
   * @param filename
   * @return
   */
  def getTempFilePath(filename: String) : String = {
    val r = new scala.util.Random
    "/" + TempFolder.value + "/" + r.nextInt(9999999) + "/" + filename
  }

  /**
   * Use to get a file Extension from Content Type ex: image/jpg
   * @param contentType
   * @return
   */
  def getExtension(contentType: Option[String]) : String = {
    contentType match {
      case Some(c) => {
        val s = c.split("/")
        if (s != null && s.length > 1) {
          s(1)
        } else {
          ".jpeg"
        }
      }
      case None => ".jpeg"
    }
  }

  def resizeImage(file: File) : Option[String]= {
    try {
      // create command
      val cmd = new ConvertCmd()
      // create the operation, add images and operators/options
      val op = new IMOperation()
      op.addImage(file.getPath)
      op.resize(320, 200)
      val smallImagePath = file.getPath + ".small"
      op.addImage(smallImagePath)
      // execute the operation
      cmd.run(op)
      Some(smallImagePath)
    } catch {
      case e:Exception => Logger.error("Image Resize Error " + e.getMessage); None
    }
  }

}
