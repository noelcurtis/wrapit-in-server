package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.Logger
import play.api.Play.current

case class PhotoRelation(ownerId: Long, photoId: Long) {

  private[this] var photo: Option[Photo] = None

  def getPhoto() : Option[Photo] = {
    photo match {
      case Some(p) => photo
      case None => { // try and get a photo
        photo = Photo.find(photoId)
        photo
      }
    }
  }

  def setPhoto(photo: Photo) = {
    this.photo = Some(photo)
  }

}

object PhotoRelation {

  /**
   * Parse a PhotoRelation from a ResultSet
   */
  val parseSingle = {
    get[Long]("photo_relation.owner_id") ~
      get[Long]("photo_relation.photo_id") map {
      case ownerId ~ photoId => PhotoRelation(ownerId, photoId)
    }
  }

  /**
   * Parse a PhotoRelation with a Photo
   */
  val parseWithPhoto = {
    PhotoRelation.parseSingle ~
    Photo.parseSingle map {
      case photoRelation ~ photo =>  {
        photoRelation.setPhoto(photo)
        photoRelation
      }
    }
  }

  val parseWithGiftListIdAndPhoto = {
    get[Long]("gift_list_role.gift_list_id") ~
      PhotoRelation.parseSingle ~
      Photo.parseSingle map {
      case giftListId ~ photoRelation ~ photo =>  {
        photoRelation.setPhoto(photo)
        (giftListId, photoRelation)
      }
    }
  }


  def create(ownerId: Long, photoId: Long): Option[PhotoRelation] = {
    try {
      DB.withConnection {
        implicit connection =>
          SQL(
            """insert into photo_relation(owner_id, photo_id)
            values({ownerId}, {photoId})"""
          ).on(
            'ownerId -> ownerId,
            'photoId -> photoId
          ).executeInsert()
          val prel = PhotoRelation(ownerId, photoId)
          Some(prel)
      }
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        None
      }
    }
  }

  def findFirst(ownerId: Long): Option[PhotoRelation] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo_relation a, photo b where a.owner_id = {ownerId} and a.photo_id = b.id limit 1").on(
          'ownerId -> ownerId
        ).as(PhotoRelation.parseWithPhoto.singleOpt)
    }
  }

  def find(ownerId: Long): List[PhotoRelation] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo_relation a, photo b where a.owner_id = {ownerId} and a.photo_id = b.id").on(
          'ownerId -> ownerId
        ).as(PhotoRelation.parseWithPhoto *)
    }
  }

  def find(ownerId: Long, photoId: Long): Option[PhotoRelation] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo_relation a, photo b where a.owner_id = {ownerId} and a.photo_id = {photoId} and a.photo_id = b.id").on(
          'ownerId -> ownerId,
          'photoId -> photoId
        ).as(PhotoRelation.parseWithPhoto.singleOpt)
    }
  }

  def findAllForGiftList(giftListId : Long) : List[PhotoRelation] = {
    DB.withConnection {
      implicit connection => {
        SQL(
          """select b.*, c.* from item a, photo_relation b, photo c
            | where gift_list_id = {giftListId} and b.owner_id = a.id
            | and b.photo_id = c.id""".stripMargin
        ).on('giftListId -> giftListId).as(PhotoRelation.parseWithPhoto *)
      }
    }
  }

  def findAllForUserGiftList(userId: Long) : List[(Long, PhotoRelation)] = {
    DB.withConnection {
      implicit connection => {
        SQL(
          """select a.gift_list_id, c.*, p.* from gift_list_role a, item b, photo_relation c, photo p where a.user_id = {userId} and a.gift_list_id = b.gift_list_id and c.owner_id = b.id and c.photo_id = p.id""".stripMargin
        ).on('userId -> userId).as(PhotoRelation.parseWithGiftListIdAndPhoto *)
      }
    }
  }


}
