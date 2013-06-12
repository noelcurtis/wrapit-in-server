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

case class Item(id: Pk[Long] = NotAssigned, name: Option[String], url: Option[String] = Some(""), needed: Option[Int] = Some(1),
                purchased: Option[Int] = Some(0), giftListId: Option[Long] = None, imgUrl: Option[String] = Some(""))

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
      get[Option[String]]("item.img_url")  map {
      case id ~ name ~ url ~ needed ~ purchased ~ giftListId ~ imgUrl => Item(id, name, url, needed, purchased, giftListId, imgUrl)
    }
  }

  def create(item: Item) : Option[Item] = {
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
            'url  -> item.url,
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

  def find(giftListId: Long) : List[Item] = {
    DB.withConnection {
      implicit connection => {
        val items : List[Item] = SQL(
          """
            select * from item where gift_list_id = {giftListId}
          """).on(
          'giftListId -> giftListId
        ).as(Item.parseSingle *)
        items
      }
    }
  }

  def findById(itemId: Long) : Option[Item] = {
    DB.withConnection {
      implicit connection => {
        val item : Option[Item] = SQL(
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
    DB.withConnection { implicit connection =>
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

}