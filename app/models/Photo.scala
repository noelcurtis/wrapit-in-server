package models

import anorm._
import anorm.SqlParser._
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import com.google.common.base.Strings
import engine.Utils

case class Photo(id: Pk[Long] = NotAssigned, folder: String, fileName: String) {

  def getPath: Option[String] = {
    if (!Strings.isNullOrEmpty(folder) && !Strings.isNullOrEmpty(fileName)) {
      Some(Utils.getAwsBucketPath + "/" + folder + "/" +fileName)
    }
    else {
      None
    }
  }
}

object Photo {

  /**
   * Parse a Photo from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("photo.id") ~
      get[String]("photo.folder") ~
      get[String]("photo.file_name") map {
      case id ~ folder ~ fileName => Photo(id, folder, fileName)
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
            """insert into photo(id, folder, file_name)
            values((select nextval('photo_seq')), {folder}, {fileName})"""
          ).on(
            'folder -> photo.folder,
            'fileName -> photo.fileName
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
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update photo
          set folder = {folder}, file_name = {fileName}
          where id = {id}
          """
        ).on(
          'folder -> photo.folder,
          'fileName -> photo.fileName,
          'id -> photo.id
        ).executeUpdate()
    }
  }

  def find(id: Long): Option[Photo] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo where id = {id}").on(
          'id -> id
        ).as(Photo.parseSingle.singleOpt)
    }
  }

  def find(folder: String, fileName: String) : List[Photo] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from photo where folder = {folder} and file_name = {fileName}").on(
          'folder -> folder,
          'fileName -> fileName
        ).as(Photo.parseSingle *)
    }
  }

  def getCount(folder: String, fileName: String): Long = {
    DB.withConnection {
      implicit connection =>
        SQL("select count(*) from photo where folder = {folder} and file_name = {fileName}").on(
          'folder -> folder,
          'fileName -> fileName
        ).as(scalar[Long].single)
    }
  }

}
