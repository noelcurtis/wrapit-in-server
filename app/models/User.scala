package models

import anorm._
import java.util.Date
import play.api.db.DB
import play.api.Play.current
import play.Logger
import com.google.common.hash.Hashing
import anorm.SqlParser._
import anorm.~

case class User(id: Pk[Long] = NotAssigned, email: Option[String], password: Option[String],
                lastSignIn: Option[Date] = None) {

}

object User {

  /**
   * Parse a User from a ResultSet
   */
  val parseSingle = {
      get[Pk[Long]]("users.id") ~
      get[Option[String]]("users.email") ~
      get[Option[String]]("users.encrypted_password") ~
      get[Option[Date]]("users.last_sign_in") map {
      case id ~ email ~ password ~ lastSignIn => User(id, email, password, lastSignIn)
    }
  }

  def create(user: User): Option[User] = {
    try {
      val hf = Hashing.sha256();
      val hpwd = hf.hashString(user.password.getOrElse(""));
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into users(id, email, encrypted_password, last_sign_in)
            values((select nextval('users_seq')), {email}, {password}, {lastSignIn})"""
          ).on(
            'id -> user.id,
            'email -> user.email,
            'password -> hpwd.toString,
            'lastSignIn -> new Date()
          ).executeInsert()

          createdId match {
            case Some(createdId) => Logger.info("Created new user: " + createdId.toString); Some(user.copy(id = anorm.Id(createdId)))
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
    DB.withConnection { implicit connection =>
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


}
