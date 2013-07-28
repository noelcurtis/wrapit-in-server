package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import play.Logger
import anorm.SqlParser._
import anorm.~
import scala.Some
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class GiftListRole(userId: Long, giftListId: Long, role: Option[Int]) {

  private[this] var giftList: Option[GiftList] = None

  /**
   * Use to get a cached version of the GiftList
   * @return
   */
  def getGiftList: Option[GiftList] = {
    this.giftList match {
      case Some(giftList) => Some(giftList) // return the gift list
      case None => {
        this.giftList = GiftList.find(giftListId) // "Lazy load" the gift list
        this.giftList
      }
    }
  }

  /**
   * Use to set the gift list
   * @param giftList
   */
  def setGiftList(giftList: Option[GiftList]) = {
    this.giftList = giftList
  }

}

object GiftListRole {

  object Role extends Enumeration {
    val Creator, Contributor, RestrictedContributor = Value

    def getInt(role: Value): Int = {
      role match {
        case Creator => 1
        case Contributor => 2
        case RestrictedContributor => 3
      }
    }
  }

  /**
   * Parse a GiftListRole from a ResultSet
   */
  val parseSingle = {
    get[Long]("gift_list_role.user_id") ~
      get[Long]("gift_list_role.gift_list_id") ~
      get[Option[Int]]("gift_list_role.role") map {
      case userId ~ giftListId ~ role => GiftListRole(userId, giftListId, role)
    }
  }


  /**
   * Parse a GiftListRole with a GiftList from a ResultSet
   */
  val parseWithGiftList = {
    GiftListRole.parseSingle ~
      GiftList.parseSingle map {
      case giftListRole ~ giftList => {
        giftListRole.setGiftList(Some(giftList))
        giftListRole
      }
    }
  }

  /**
   * Use to write a GiftListRole to JSON String
   */
  implicit val writesGiftListRole : Writes[GiftListRole] = (
      (__ \ 'role).write[Option[Int]] and
        (__ \ 'giftList).write[Option[GiftList]]
    )(r => (r.role, r.getGiftList))



  def create(userId: Long, giftListId: Long, role: Option[Int]): Option[GiftListRole] = {
    try {
      DB.withConnection {
        implicit connection =>
          SQL(
            """insert into gift_list_role(user_id, gift_list_id, role)
            values({userId}, {giftListId}, {role})"""
          ).on(
            'userId -> userId,
            'giftListId -> giftListId,
            'role -> role
          ).executeInsert()
          val giftListRole = GiftListRole(userId, giftListId, role)
          Some(giftListRole)
      }
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        None
      }
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

  def find(userId: Long): List[GiftListRole] = {
    DB.withConnection {
      implicit connection => {
        val roles: List[GiftListRole] = SQL(
          """
            select * from gift_list_role
            left join gift_list on gift_list_role.gift_list_id = gift_list.id
            where gift_list_role.user_id = {userId}
          """).on(
          'userId -> userId
        ).as(GiftListRole.parseWithGiftList *)
        roles
      }
    }
  }

}
