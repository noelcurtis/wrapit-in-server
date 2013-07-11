package models

import anorm.SqlParser._
import engine.ItemRelationType
import anorm._
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current

case class ItemRelation (userId: Long, itemId: Long, relationType: ItemRelationType) {

  var item: Item = null

  def setItem(item: Item) = {
    this.item = item
  }

  def getItem : Item = {
    this.item
  }

}


object ItemRelation {

  /**
   * Parse a Item from a ResultSet
   */
  val parseSingle = {
    get[Long]("user_item_relation.user_id") ~
      get[Long]("user_item_relation.item_id") ~
      get[Int]("user_item_relation.r_type") map {
      case userId ~ itemId ~ rType => ItemRelation(userId, itemId, ItemRelationType.fromInt(rType))
    }
  }

  def create(itemRelation: ItemRelation): Option[ItemRelation] = {
    try {
      Logger.info("Creating ItemRelation " + itemRelation.toString)
      DB.withTransaction {
        implicit connection =>
          SQL(
            """insert into user_item_relation(user_id, item_id, r_type)
            values({userId}, {itemId}, {rType})"""
          ).on(
            'userId -> itemRelation.userId,
            'itemId -> itemRelation.itemId,
            'rType -> itemRelation.relationType.value
          ).executeInsert()

          // find the item to ensure it has been created
          val created: Option[ItemRelation] = SQL(
            """select * from user_item_relation where user_id = {userId} and item_id = {itemId} and r_type = {relationType}""").on(
            'userId -> itemRelation.userId,
            'itemId -> itemRelation.itemId,
            'relationType -> itemRelation.relationType.value
          ).as(ItemRelation.parseSingle.singleOpt)

          created match {
            case Some(c) => created
            case None => Logger.error("ItemRelation not created " + itemRelation); None
          }
      }
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        None
      }
    }
  }

  def delete(userId: Long, itemId: Long, relationType: ItemRelationType) = {
    DB.withConnection {
      implicit connection => {
        SQL("delete from user_item_relation where user_id = {userId} and item_id = {itemId} and r_type = {relationType}").on(
          'userId -> userId,
          'itemId -> itemId,
          'relationType -> relationType.value
        ).execute()
      }
    }
  }

  def find(userId: Long, itemId: Long, relationType: ItemRelationType): Option[ItemRelation] = {
    DB.withConnection {
      implicit connection => {
        val item: Option[ItemRelation] = SQL(
          """select * from user_item_relation where user_id = {userId} and item_id = {itemId} and r_type = {relationType}""").on(
          'userId -> userId,
          'itemId -> itemId,
          'relationType -> relationType.value
        ).as(ItemRelation.parseSingle.singleOpt)
        item
      }
    }
  }


  def find(itemId: Long, relationType: ItemRelationType) : Option[ItemRelation] = {
    DB.withConnection {
      implicit connection => {
        val item: Option[ItemRelation] = SQL(
          """
            |select * from user_item_relation where item_id = {itemId} and r_type = {relationType}
          """.stripMargin).on('itemId -> itemId, 'relationType -> relationType.value).as(ItemRelation.parseSingle.singleOpt)
        item
      }
    }
  }

}
