package models

import anorm._
import java.util.Date
import play.api.db.DB
import play.Logger
import anorm.SqlParser._
import anorm.~
import scala.Some
import play.api.Play.current

case class GiftList(id: Pk[Long] = NotAssigned, name: Option[String], purpose: Option[String],
                    dueDate: Option[Date])


object GiftList {

  /**
   * Parse a User from a ResultSet
   */
  val parseSingle = {
       get[Pk[Long]]("gift_list.id") ~
      get[Option[String]]("gift_list.name") ~
      get[Option[String]]("gift_list.purpose") ~
      get[Option[Date]]("gift_list.due_date") map {
      case id ~ name ~ purpose ~ dueDate => GiftList(id, name, purpose, dueDate)
    }
  }

  def create(giftList: GiftList): Option[GiftList] = {
    try {
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into gift_list(id, name, purpose, due_date)
            values((select nextval('gift_list_seq')), {name}, {purpose}, {dueDate})"""
          ).on(
            'id -> giftList.id,
            'name -> giftList.name,
            'purpose -> giftList.purpose,
            'dueDate -> giftList.dueDate
          ).executeInsert()

          createdId match {
            case Some(createdId) => Some(giftList.copy(id = anorm.Id(createdId)))
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

  def find(id: Long): Option[GiftList] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from gift_list where id = {id}").on('id -> id).as(GiftList.parseSingle.singleOpt)
    }
  }

}
