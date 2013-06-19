package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import org.joda.time.DateTime
import play.Logger
import play.api.Play.current

case class FbInfo (userId: Long, token: String, fbUserId: Long, expiresAt: Long) {

  def isExpired:Boolean = {
    val eDate = new DateTime(expiresAt)
    if (eDate.isBeforeNow) true else false
  }

}

object FbInfo {

  /**
   * Parse a FbInfo from a ResultSet
   */
  val parseSingle = {
    get[Long]("fb_info.user_id") ~
      get[String]("fb_info.token") ~
      get[Long]("fb_info.fb_user_id") ~
      get[Long]("fb_info.expires_at") map {
      case userId ~ token ~ fbUserId ~ expiresAt => FbInfo(userId, token, fbUserId, expiresAt)
    }
  }

  def create(fbInfo: FbInfo): Option[FbInfo] = {
    try {
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into fb_info(user_id, token, fb_user_id, expires_at)
            values({userId}, {token}, {fbUserId}, {expiresAt})"""
          ).on(
            'userId -> fbInfo.userId,
            'token -> fbInfo.token,
            'fbUserId -> fbInfo.fbUserId,
            'expiresAt -> fbInfo.expiresAt
          ).executeInsert()

          createdId match {
            case Some(createdId) => Logger.info("Created FB info for user " + createdId.toString); Some(fbInfo.copy(userId = createdId))
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


  def update(fbInfo: FbInfo) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update fb_info
          set token = {token}, fbUserId = {fb_user_id}, expiresAt = {expires_at}
          where user_id = {userId}
          """
        ).on(
          'userId -> fbInfo.userId,
          'token -> fbInfo.token,
          'fbUserId -> fbInfo.fbUserId,
          'expiresAt -> fbInfo.expiresAt
        ).executeUpdate()
    }
  }

  /**
   * Use to get FbInfo for a User
   * @param userId
   * @return
   */
  def find(userId: Long): Option[FbInfo] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from fb_info where user_id = {userId}").on('userId -> userId).as(FbInfo.parseSingle.singleOpt)
    }
  }

  def findByFacebookId(fbId: Long): Option[FbInfo] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from fb_info where fb_user_id = {fbUserId}").on('fbUserId -> fbId).as(FbInfo.parseSingle.singleOpt)
    }
  }

}
