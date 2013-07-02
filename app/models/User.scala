package models

import anorm._
import java.util.{UUID, Date}
import play.api.db.DB
import play.api.Play.current
import play.Logger
import com.google.common.hash.Hashing
import anorm.SqlParser._
import anorm.~
import play.api.cache.Cache

case class User(id: Pk[Long] = NotAssigned, email: Option[String], password: Option[String],
                lastSignIn: Option[Date] = None, token: Option[String] = Some("")) {

  def getFacebookInfo:Option[FbInfo] = {
    FbInfo.find(id.get)
  }

}

object User {

  /**
   * Parse a User from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("users.id") ~
      get[Option[String]]("users.email") ~
      get[Option[String]]("users.encrypted_password") ~
      get[Option[Date]]("users.last_sign_in") ~
      get[Option[String]]("users.token") map {
      case id ~ email ~ password ~ lastSignIn ~ token => User(id, email, password, lastSignIn, token)
    }
  }

  def create(user: User, fakeToken: Option[String] = None): Option[User] = {
    try {
      val hf = Hashing.sha256();
      val hpwd = hf.hashString(user.password.getOrElse(""));
      val token = if (fakeToken.isDefined) fakeToken.get else UUID.randomUUID().toString
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into users(id, email, encrypted_password, last_sign_in, token)
            values((select nextval('users_seq')), {email}, {password}, {lastSignIn}, {token})"""
          ).on(
            'id -> user.id,
            'email -> user.email,
            'password -> hpwd.toString,
            'lastSignIn -> new Date(),
            'token -> token.toString
          ).executeInsert()

          createdId match {
            case Some(createdId) => Logger.info("Created new user: " + createdId.toString); Some(user.copy(id = anorm.Id(createdId), token = Some(token)))
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
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    val hf = Hashing.sha256();
    val hpwd = hf.hashString(password);
    DB.withConnection {
      implicit connection =>
        SQL(
          """
         select * from users where
         email = {email} and encrypted_password = {password}
          """
        ).on(
          'email -> email,
          'password -> hpwd.toString
        ).as(User.parseSingle.singleOpt)
    }
  }


  def find(id: Long): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from users where id = {id}").on('id -> id).as(User.parseSingle.singleOpt)
    }
  }

  def find(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from users where email = {email}").on('email -> email).as(User.parseSingle.singleOpt)
    }
  }

  def findByToken(token: String): Option[User] = {
    Cache.getOrElse[Option[User]](token){
      DB.withConnection {
        implicit connection =>
          SQL("select * from users where token = {token}").on('token -> token).as(User.parseSingle.singleOpt)
      }
    }
  }

  def findByFacebookId(fbId: Long): Option[User] = {
    val foundFbInfo = FbInfo.findByFacebookId(fbId)
    foundFbInfo match {
      case Some(f) => find(f.userId)
      case None => None
    }
  }

}
