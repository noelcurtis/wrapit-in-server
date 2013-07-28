package models

import anorm._
import anorm.SqlParser._
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
import engine.{ItemsFolder, Creator, Utils}

case class Item(id: Pk[Long] = NotAssigned, name: Option[String], url: Option[String] = Some(""), needed: Option[Int] = Some(1),
                purchased: Option[Int] = Some(0), giftListId: Option[Long] = None) {

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
      get[Option[Long]]("item.gift_list_id") map {
      case id ~ name ~ url ~ needed ~ purchased ~ giftListId => Item(id, name, url, needed, purchased, giftListId)
    }
  }

  def create(item: Item): Option[Item] = {
    try {
      Logger.info("Creating Item " + item.toString)
      DB.withConnection {
        implicit connection => {
          val createdId: Option[Long] = SQL(
            """insert into item(id, gift_list_id, name, url, needed, purchased)
            values((select nextval('item_seq')), {giftListId}, {name}, {url}, {needed}, {purchased})"""
          ).on(
            'giftListId -> item.giftListId,
            'name -> item.name,
            'url -> item.url,
            'needed -> item.needed,
            'purchased -> item.purchased
          ).executeInsert()

          createdId match {
            case Some(createdId) => Some(item.copy(id = anorm.Id(createdId)))
            case None => None
          }
        }
      }
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        None
      }
    }
  }

  def createWithRelation(item: Item, user: User): Option[ItemRelation] = {
    DB.withTransaction {
      implicit connection => {
      // create the Item
        val createdId: Option[Long] = SQL(
          """insert into item(id, gift_list_id, name, url, needed, purchased)
            values((select nextval('item_seq')), {giftListId}, {name}, {url}, {needed}, {purchased})"""
        ).on(
          'giftListId -> item.giftListId,
          'name -> item.name,
          'url -> item.url,
          'needed -> item.needed,
          'purchased -> item.purchased
        ).executeInsert()

        createdId match {
          case Some(createdId) => {
            val newItem = item.copy(id = anorm.Id(createdId))
            // create the ItemRelation
            SQL(
              """insert into user_item_relation(user_id, item_id, r_type)
            values({userId}, {itemId}, {rType})"""
            ).on(
              'userId -> user.id.get,
              'itemId -> createdId,
              'rType -> Creator.value
            ).executeInsert()

            // find the ItemRelation to ensure it has been created
            val createdRelation: Option[ItemRelation] = SQL(
              """select * from user_item_relation where user_id = {userId} and item_id = {itemId} and r_type = {relationType}""").on(
              'userId -> user.id.get,
              'itemId -> createdId,
              'relationType -> Creator.value
            ).as(ItemRelation.parseSingle.singleOpt)

            createdRelation match {
              case Some(c) => c.setItem(newItem); Some(c)
              case None => Logger.error(s"ItemRelation not created user: ${user.id.get} itemId: ${createdId} {Creator}"); None
            }
          }
          case None => None
        }
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
          set name = {name}, url = {url}, needed = {needed}, purchased = {purchased}
          where id = {id}
          """
        ).on(
          'name -> item.name,
          'url -> item.url,
          'needed -> item.needed,
          'purchased -> item.purchased,
          'id -> item.id
        ).executeUpdate()
    }
  }

  val cacheTime = 864000
  val defaultContentType = "image/jpg"

  def addPhoto(item: Item, externalUrl: String, withUpload: Boolean = true) = {
    try {
      val url = new URL(externalUrl)
      //val extension = url.getFile.dropWhile(_ != '.').replaceFirst(".", "") // get the file extension
      Logger.info(s"Getting Photo {$externalUrl} for Item {" + item.id + "} started");
      // get the file and push to S3
      WS.url(externalUrl).get().map {
        r => {
          val contentType = r.getAHCResponse.getContentType
          val bytes = r.getAHCResponse.getResponseBodyAsBytes
          if (r.getAHCResponse.getStatusCode != HttpStatus.SC_OK) {
            Logger.error(s"Could not find image at URL $externalUrl")
          } else {
            Logger.info(s"Getting Photo {$externalUrl} for Item {$item} ended");
            val path = Utils.getAwsFilePath(bytes, ItemsFolder, Utils.getExtension(Some(contentType)))

            // check if file already exists
            val foundPhotos = Photo.find(path._1, path._2)
            if (withUpload) {
              if (foundPhotos.size > 0) {
                Logger.info(s"File already exists for folder $path")
                PhotoRelation.create(item.id.get, foundPhotos.head.id.get) // Add the Photo to the Item
              } else {
                Logger.info(s"File does not exist for folder $path");
                uploadPhoto(path._1, path._2, contentType, bytes, item)
              }
            } else {
              // FAKE THE UPLOAD
              fakeCreate(path._1, path._2, item)
            }
          }
        }
      }
    } catch {
      case e: Exception => Logger.info(s"Error adding Photo {$externalUrl} to Item {" + item.id + "}" + e.getMessage)
    }
  }

  def addPhoto(item: Item, bytes: Array[Byte], contentType: Option[String]) = {
    val path = Utils.getAwsFilePath(bytes, ItemsFolder, Utils.getExtension(contentType))
    Logger.info(s"Photo path $path")
    val foundPhotos = Photo.find(path._1, path._2)
    // check if the file already exists
    if (foundPhotos.size > 0) {
      Logger.info(s"File already exists for folder $path")
      PhotoRelation.create(item.id.get, foundPhotos.head.id.get) // Add the Photo to the Item
    } else {
      Logger.info(s"File does not exist for folder $path")
      uploadPhoto(path._1, path._2, contentType.getOrElse(defaultContentType), bytes, item)
    }
  }

  private def uploadPhoto(folder: String, filename: String, contentType: String, bytes: Array[Byte], item: Item) = {
    Logger.info(s"Uploading Photo to AWS for Item {$item} started");
    val bucket = S3(Utils.getAwsBucket) // get the bucket
    val awsUpload = bucket + BucketFile(folder + "/" + filename, contentType, bytes, None, Some(Map("Cache-Control" -> s"max-age=$cacheTime, must-revalidate")))
    awsUpload.map {
      case Left(error) => throw new Exception("AWS upload error " + error.originalXml.toString)
      case Right(success) => {
        val newPhoto = Photo.create(Photo(folder = folder, fileName = filename))
        newPhoto match {
          case Some(photo) => {
            PhotoRelation.create(item.id.get, photo.id.get) // Add the Photo to the Item
            Logger.info("Uploading Photo to AWS for Item {" + item.id + "} ended");
          }
          case None => Logger.error("Photo could not be created " + newPhoto.toString)
        }
      }
    }
  }

  private def fakeCreate(folder: String, filename: String, item: Item) {
    Logger.info("Fake creating Photo");
    val newPhoto = Photo.create(Photo(folder = folder, fileName = filename))
    newPhoto match {
      case Some(photo) => {
        PhotoRelation.create(item.id.get, photo.id.get) // Add the Photo to the Item
      }
      case None => Logger.error("Fake Create photo could not be created " + newPhoto.toString)
    }
  }

}