package models

import anorm._
import anorm.SqlParser._
import play.Logger
import play.api.db.DB
import anorm.~
import scala.Some
import java.util.Date
import play.api.Play.current

case class CommentRelation (id : Pk[Long] = NotAssigned, commentId: Long, userId: Long,
                            itemId: Long, createdAt: Date = new Date()) {

  private[this] var comment: Option[Comments] = None // ref to comment

  def getComment() : Option[Comments] = {
    comment match {
      case Some(c) => comment
      case None => {
        // lazy fetch the comment
        val found = Comments.find(commentId)
        found match {
          case Some(c) => comment = Some(c); comment
          case None => Logger.error("No Comments for CommentRelation " + this.toString); None
        }
      }
    }
  }

  def setComment(comment: Comments) = {
    this.comment = Some(comment)
  }

}


object CommentRelation {

  /**
   * Parse a CommentRelation from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("comment_relation.id") ~
      get[Long]("comment_relation.comment_id") ~
      get[Long]("comment_relation.user_id") ~
      get[Long]("comment_relation.item_id") ~
      get[Date]("comment_relation.created_at") map {
      case id ~ commentId ~ userId ~ itemId ~ createdAt => CommentRelation(id, commentId, userId, itemId, createdAt)
    }
  }

  def create(commentRelation : CommentRelation) : Option[CommentRelation] = {
    try{
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into comment_relation(id, comment_id, user_id, item_id, created_at)
            values((select nextval('comment_relation_seq')), {commentId}, {userId}, {itemId}, {createdAt})"""
          ).on(
            'commentId -> commentRelation.commentId,
            'userId -> commentRelation.userId,
            'itemId -> commentRelation.itemId,
            'createdAt -> new Date()
          ).executeInsert()

          createdId match {
            case Some(createdId) => {
              Logger.info("Created new CommentRelation: " + createdId.toString)
              Some(commentRelation.copy(id = anorm.Id(createdId)))
            }
            case None => None
          }
      }
    } catch {
      case e:Exception => Logger.error("Could not create Comment Relation " + commentRelation.toString); None
    }
  }

}