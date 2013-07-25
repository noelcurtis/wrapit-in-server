package models

import anorm._
import java.util.Date
import play.api.db.DB
import play.Logger
import anorm.SqlParser._
import anorm.~
import scala.Some
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class GiftList(id: Pk[Long] = NotAssigned, name: Option[String], purpose: Option[String],
                    dueDate: Option[Date]) {

  private[this] var itemCount : Option[Int] = None;

  //TODO: update to NOT cache itemCount !! only Lazy Load
  def getItemCount : Int = {
    itemCount.getOrElse(id match {
      case NotAssigned => itemCount = Some(0); itemCount.get
      case _ => val items = Item.find(id.get); itemCount = Some(items.size); itemCount.get
    })
  }

}


object GiftList {

  /**
   * Parse a User from a ResultSet
   */
  val parseSingle = {
    get[Pk[Long]]("gift_list.id") ~
      get[Option[String]]("gift_list.name") ~
      get[Option[String]]("gift_list.purpose") ~
      get[Option[Date]]("gift_list.due_date") map {
      case id ~ name ~ purpose ~ dueDate => GiftList(id, name, purpose, dueDate)
    }
  }

  /**
   * Use to read a GiftList from JSON
   */
  implicit val readGiftList : Reads[GiftList] = (
    (__ \ 'id).readNullable[Long].map[Pk[Long]](x => if(x.isDefined) anorm.Id(x.get) else NotAssigned) and
      (__ \ 'name).readNullable[String] and
      (__ \ 'purpose).readNullable[String] and
      (__ \ 'dueDate).readNullable[Date]
    )(GiftList.apply _)

  /**
   * Use to write a GiftList to JSON String
   */
  implicit val writesGiftList : Writes[GiftList] = (
    (__ \ 'id).write[Long] and
      (__ \ 'name).write[Option[String]] and
      (__ \ 'dueDate).write[Option[Date]] and
      (__ \ 'itemCount).write[Int]
    )(x => (x.id.get, x.name, x.dueDate, x.getItemCount))

  /**
   * Use to create a Gift List
   * @param giftList
   * @return
   */
  private def create(giftList: GiftList): Option[GiftList] = {
    try {
      Logger.info("Creating GiftList " + giftList.toString)
      DB.withConnection {
        implicit connection =>
          val createdId: Option[Long] = SQL(
            """insert into gift_list(id, name, purpose, due_date)
            values((select nextval('gift_list_seq')), {name}, {purpose}, {dueDate})"""
          ).on(
            'id -> giftList.id,
            'name -> giftList.name,
            'purpose -> giftList.purpose,
            'dueDate -> giftList.dueDate
          ).executeInsert()

          createdId match {
            case Some(createdId) => Some(giftList.copy(id = anorm.Id(createdId)))
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
   * Use to create a GiftList for a user,
   * User will be assigned as Creator of the GiftList
   * @param giftList
   * @param userId
   * @return
   */
  def create(giftList: GiftList, userId: Long): Option[GiftListRole] = {
    Logger.info("Creating GiftList " + giftList.toString + " for User {" + userId + "}")
    val nList = create(giftList) // create the gift list
    nList match {
      case Some(nList) => {
        val glRole = GiftListRole.create(userId, nList.id.get, Some(GiftListRole.Role.getInt(GiftListRole.Role.Creator)))
        Logger.info("Creating GiftList role success")
        glRole
      }
      case None => None
    }
  }

  def find(id: Long): Option[GiftList] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from gift_list where id = {id}").on('id -> id).as(GiftList.parseSingle.singleOpt)
    }
  }

  /**
   * Use to add an item to a gift list
   * @param item
   * @return
   */
  def addItem(item: Item, user: User, giftListId: Long): Option[Long] = {
    Logger.info(s"Adding $item to $giftListId")
    val nItem = item.copy(giftListId = Some(giftListId))
    val cItemRelation = Item.createWithRelation(nItem, user)
    cItemRelation match {
      case Some(ir) => Some(ir.getItem.id.get)
      case None => {
        Logger.error("Failed to add item " + item.toString + " to list " + giftListId)
        None
      }
    }
  }

}
