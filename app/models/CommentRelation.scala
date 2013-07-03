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
  private[this] var user: Option[User] = None // ref to user

  def getComment() : Option[Comments] = {
    comment match {
      case Some(c) => comment // if comment is set just return it
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

  def getUser() : Option[User] = {
    user match {
      case Some(u) => user
      case None => {
        // lazy fetch the comment
        val found = User.find(userId)
        found match {
          case Some(u) => user = Some(u); user
          case None => Logger.error("No User for CommentRelation " + this.toString); None
        }
      }
    }
  }

  def setUser(user: User) = {
    this.user = Some(user)
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


  /**
   * Parse a CommentRelation with a User and Comment from a ResultSet
   */
  val parseWithUserComment = {
    CommentRelation.parseSingle ~
      User.parseSingle ~
      Comments.parseSingle map {
      case commentRelation ~ user ~comments => {
        commentRelation.setUser(user)
        commentRelation.setComment(comments)
        commentRelation
      }
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
      case e:Exception => Logger.error("Could not create Comment Relation " + commentRelation.toString + " " + e.getMessage); None
    }
  }

  /**
   * Use to find all comments by a User for an Item
   * @param userId
   * @param itemId
   * @return
   */
  def find(userId: Long, itemId: Long) : List[CommentRelation] = {
    DB.withConnection {
      implicit connection => {
        val results: List[CommentRelation] = SQL(
          """
            select * from comment_relation a, users b, comments c
            where a.comment_id = c.id and a.user_id = b.id and a.user_id = {userId} and a.item_id = {itemId}
            order by a.created_at desc
          """).on(
          'userId -> userId,
          'itemId -> itemId
        ).as(CommentRelation.parseWithUserComment *)
        results
      }
    }
  }

  /**
   * Use to find all Comments for an Item
   * @param itemId
   * @return
   */
  def find(itemId: Long) : List[CommentRelation] = {
    DB.withConnection {
      implicit connection => {
        val results: List[CommentRelation] = SQL(
          """
            select * from comment_relation a, users b, comments c
            where a.comment_id = c.id and a.user_id = b.id and a.item_id = {itemId}
            order by a.created_at desc
          """).on(
          'itemId -> itemId
        ).as(CommentRelation.parseWithUserComment *)
        results
      }
    }
  }

}