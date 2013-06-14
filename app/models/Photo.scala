package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current

case class Photo(id: Pk[Long] = NotAssigned, folder: String, path: String)

object Photo {

  /**
   * Parse a Photo from a ResultSet
   */
  val parseSingle = {
      get[Pk[Long]]("photo.id") ~
      get[String]("photo.folder") ~
      get[String]("photo.path") map {
      case id ~ folder ~ path => Photo(id, folder, path)
    }
  }

  /**
   * Use to create a Photo
   * @param photo
   * @return
   */
  def create(photo: Photo): Option[Photo] = {
    try {
      Logger.info("Creating Photo " + photo.toString)
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into photo(id, folder, path)
            values((select nextval('photo_seq')), {folder}, {path})"""
          ).on(
            'folder -> photo.folder,
            'path -> photo.path
          ).executeInsert()

          createdId match {
            case Some(createdId) => Some(photo.copy(id = anorm.Id(createdId)))
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

  /**
   * Use to update a Photo
   * @param photo
   * @return
   */
  def update(photo: Photo) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update photo
          set folder = {folder}, path = {path}
          where id = {id}
        """
      ).on(
        'folder -> photo.folder,
        'path -> photo.path,
        'id -> photo.id
      ).executeUpdate()
    }
  }

}
