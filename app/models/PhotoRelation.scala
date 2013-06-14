package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.Logger
import play.api.Play.current

case class PhotoRelation (ownerId: Long, photoId: Long)

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


  def create(ownerId: Long, photoId: Long) : Option[PhotoRelation] = {
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

  def find(ownerId: Long, photoId: Long): Option[PhotoRelation] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo_relation where owner_id = {ownerId} and photo_id = {photoId}").on(
          'ownerId -> ownerId,
          'photoId -> photoId
        ).as(PhotoRelation.parseSingle.singleOpt)
    }
  }


}
