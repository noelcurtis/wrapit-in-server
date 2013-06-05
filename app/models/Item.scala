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

case class Item(id: Pk[Long] = NotAssigned, name: Option[String], needed: Option[Int] = Some(1),
                purchased: Option[Int] = Some(0), giftListId: Option[Long] = None )

object Item {

  /**
   * Parse a Item from a ResultSet
   */
  val parseSingle = {
      get[Pk[Long]]("item.id") ~
      get[Option[String]]("item.name") ~
      get[Option[Int]]("item.needed") ~
      get[Option[Int]]("item.purchased") ~
      get[Option[Long]]("item.gift_list_id") map {
      case id ~ name ~ needed ~ purchased ~ giftListId => Item(id, name, needed, purchased, giftListId)
    }
  }

  def create(item: Item) : Option[Item] = {
    try {
      Logger.info("Creating GiftList " + item.toString)
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into item(id, gift_list_id, name, needed, purchased)
            values((select nextval('item_seq')), {giftListId}, {name}, {needed}, {purchased})"""
          ).on(
            'giftListId -> item.giftListId,
            'name -> item.name,
            'needed -> item.needed,
            'purchased -> item.purchased
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

  def find(giftListId: Long) : List[Item] = {
    DB.withConnection {
      implicit connection => {
        val roles : List[Item] = SQL(
          """
            select * from item where gift_list_id = {giftListId}
          """).on(
          'giftListId -> giftListId
        ).as(Item.parseSingle *)
        roles
      }
    }
  }

}