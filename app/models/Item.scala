package models

import anorm._
import anorm.SqlParser._
import scala.Some
import java.util.Date
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import play.api.libs.ws.WS
import java.net.URL
import fly.play.s3.{BucketFile, S3}
import play.api.libs.concurrent.Execution.Implicits._
import org.apache.http.HttpStatus
import com.google.common.hash.Hashing
import engine.Utils

case class Item(id: Pk[Long] = NotAssigned, name: Option[String], url: Option[String] = Some(""), needed: Option[Int] = Some(1),
                purchased: Option[Int] = Some(0), giftListId: Option[Long] = None, imgUrl: Option[String] = Some("")) {

  /**
   * Use to get a photo for an Item
   * @return
   */
  def getPhoto: Option[Photo] = {
    PhotoRelation.findFirst(id.get) match {
      case Some(relation) => Photo.find(relation.photoId)
      case None => Logger.info("No photos for item " + this.toString); None
    }
  }

}

object Item {

  /**
   * Parse a Item from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("item.id") ~
      get[Option[String]]("item.name") ~
      get[Option[String]]("item.url") ~
      get[Option[Int]]("item.needed") ~
      get[Option[Int]]("item.purchased") ~
      get[Option[Long]]("item.gift_list_id") ~
      get[Option[String]]("item.img_url") map {
      case id ~ name ~ url ~ needed ~ purchased ~ giftListId ~ imgUrl => Item(id, name, url, needed, purchased, giftListId, imgUrl)
    }
  }

  def create(item: Item): Option[Item] = {
    try {
      Logger.info("Creating Item " + item.toString)
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into item(id, gift_list_id, name, url, needed, purchased, img_url)
            values((select nextval('item_seq')), {giftListId}, {name}, {url}, {needed}, {purchased}, {imgUrl})"""
          ).on(
            'giftListId -> item.giftListId,
            'name -> item.name,
            'url -> item.url,
            'needed -> item.needed,
            'purchased -> item.purchased,
            'imgUrl -> item.imgUrl
          ).executeInsert()

          createdId match {
            case Some(createdId) => Some(item.copy(id = anorm.Id(createdId)))
            case None => None
          }
      }
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        None
      }
    }
  }

  def find(giftListId: Long): List[Item] = {
    DB.withConnection {
      implicit connection => {
        val items: List[Item] = SQL(
          """
            select * from item where gift_list_id = {giftListId}
          """).on(
          'giftListId -> giftListId
        ).as(Item.parseSingle *)
        items
      }
    }
  }

  def findById(itemId: Long): Option[Item] = {
    DB.withConnection {
      implicit connection => {
        val item: Option[Item] = SQL(
          """
            select * from item where id = {itemId}
          """).on(
          'itemId -> itemId
        ).as(Item.parseSingle.singleOpt)
        item
      }
    }
  }

  def update(item: Item) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update item
          set name = {name}, url = {url}, needed = {needed}, purchased = {purchased}, img_url = {imgUrl}
          where id = {id}
          """
        ).on(
          'name -> item.name,
          'url -> item.url,
          'needed -> item.needed,
          'purchased -> item.purchased,
          'imgUrl -> item.imgUrl,
          'id -> item.id
        ).executeUpdate()
    }
  }


  val cacheTime = 864000

  def addPhoto(item: Item, externalUrl: String) = {
    try {
      Logger.info(s"Adding Photo {$externalUrl} to Item {$item} started");
      val url = new URL(externalUrl)
      val extension = url.getFile.dropWhile(_ != '.').replaceFirst(".", "") // get the file extension
      // get the file and push to S3
      WS.url(externalUrl).get().map {
        r => {
          val contentType = r.getAHCResponse.getContentType
          val bytes = r.getAHCResponse.getResponseBodyAsBytes
          if (r.getAHCResponse.getStatusCode != HttpStatus.SC_OK) {
            Logger.error(s"Could not find image at URL $externalUrl")
          }
          else {
            val hash = Hashing.sha256().hashBytes(bytes).toString // create a hash code from the array
            val folder = "items"
            val filename = hash + extension
            val bucket = S3(Utils.getAwsBucket) // get the bucket
            val awsUpload = bucket + BucketFile(folder + "/" + filename, contentType, bytes, None, Some(Map("Cache-Control" -> s"max-age=$cacheTime, must-revalidate")))
            awsUpload.map {
              case Left(error) => throw new Exception("AWS upload error " + error.originalXml.toString)
              case Right(success) => {
                val newPhoto = Photo.create(Photo(folder = folder, path = filename))
                newPhoto match {
                  case Some(photo) => {
                    PhotoRelation.create(item.id.get, photo.id.get) // Add the Photo to the Item
                    Logger.info(s"Adding Photo {$externalUrl} to Item {$item} ended");
                  }
                  case None => Logger.error("Photo could not be created " + newPhoto.toString)
                }
              }
            }
          }
        }
      }
    } catch {
      case e: Exception => Logger.info(s"Error adding Photo {$externalUrl} to Item {$item} " + e.getMessage)
    }
  }

}