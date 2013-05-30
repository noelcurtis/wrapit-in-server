package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import play.Logger
import anorm.SqlParser._

case class GiftListRole(userId: Long, giftListId: Long, role: Option[Int])

object GiftListRole {

  object Role extends Enumeration {
    val Creator, Contributor, RestrictedContributor = Value

    def getInt (role:Value) : Int = {
      role match {
        case Creator => 1
        case Contributor => 2
        case RestrictedContributor => 3
      }
    }
  }

  /**
   * Parse a User from a ResultSet
   */
  val parseSingle = {
      get[Long]("gift_list_role.user_id") ~
      get[Long]("gift_list_role.gift_list_id") ~
      get[Option[Int]]("gift_list_role.role") map {
      case userId ~ giftListId ~ role => GiftListRole(userId, giftListId, role)
    }
  }

  def create(role: GiftListRole) = {
    try {
      DB.withConnection {
        implicit connection =>
          SQL(
            """insert into gift_list_role(user_id, gift_list_id, role)
            values({userId}, {giftListId}, {role})"""
          ).on(
            'userId -> role.userId,
            'giftListId -> role.giftListId,
            'role -> role.role
          ).executeInsert()
      }
    } catch {
      case e: Exception => Logger.error(e.getMessage)
    }
  }

  def find(userId: Long, giftListId: Long): Option[GiftListRole] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from gift_list_role where user_id = {userId} and gift_list_id = {giftListId}").on(
          'userId -> userId,
          'giftListId -> giftListId
        ).as(GiftListRole.parseSingle.singleOpt)
    }
  }

}
