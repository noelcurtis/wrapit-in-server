package models

import anorm._
import anorm.SqlParser._
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import java.util.Date
import play.api.Play.current

case class Comments(id : Pk[Long] = NotAssigned, note: String) {

}


object Comments {

  /**
   * Parse a Comments from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("comments.id") ~
      get[String]("comments.notes") map {
      case commentId ~ note => Comments(commentId, note)
    }
  }

  def create(comment: String) : Option[Comments] = {
    try{
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into comments(id, note)
            values((select nextval('comments_seq')), {note})"""
          ).on(
            'note -> comment
          ).executeInsert()

          createdId match {
            case Some(createdId) => {
              Logger.info("Created new comment: " + createdId.toString)
              Some(Comments(anorm.Id(createdId), comment))
            }
            case None => None
          }
      }
    } catch {
      case e: Exception => Logger.error(e.getMessage)
        None
    }
  }

  def find(id: Long): Option[Comments] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from comments where id = {id}").on('id -> id).as(Comments.parseSingle.singleOpt)
    }
  }

  def create(userId: Long, itemId: Long, comment: String) : Option[CommentRelation] = {
    // create the comment
    create(comment) match {
      case Some(c) => {
        // create the comment relation
        val relation = CommentRelation.create(CommentRelation(commentId = c.id.get, userId = userId, itemId = itemId))
        relation match {
          case Some(r) => {
            r.setComment(c) // set the comment
            Some(r) // return
          }
          case None => Logger.error("Could not create CommentRelation for Comments " + c.id.toString + " " + userId + " " + itemId); None
        }
      }
      case None => Logger.error("Could not create Comments "+ userId + " " + itemId); None
    }
  }

}
