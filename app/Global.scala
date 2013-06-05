import org.joda.time.DateTime
import play.api._

import models._
import anorm._
import java.util.Date
import play.api.db.DB
import play.Logger
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.debug("Application start")
    if (play.Play.isDev) // seed data in dev mode
    {
      InitialData.cleanDb
      InitialData.insert
    }
  }

  override def onStop(app: Application) {

  }

}

/**
 * Initial set of data to be imported
 * in the sample application.
 */
object InitialData {

  def insert = {
    Logger.debug("Creating test data.");
    // create some users
    val foobar = User.create(User(NotAssigned, Some("foobar@gmail.com"), Some("foobar"), Some(new Date())))
    val foobar1 = User.create(User(NotAssigned, Some("foobar1@gmail.com"), Some("foobar1"), Some(new Date())))

    val dateInFuture = new DateTime().plusDays(42);

    // create some gift lists for users
    val foobarList = GiftList(NotAssigned, Some("A List by Foobar"), Some("Something interesting about this list"), Some(dateInFuture.toDate))
    val foobarList1 = GiftList(NotAssigned, Some("A List For Ann"), Some("Something interesting about this list"), Some(dateInFuture.plusDays(4).toDate))
    val foobarList2 = GiftList(NotAssigned, Some("A List For Joe"), Some("Something interesting about this list"), Some(dateInFuture.plusDays(365).toDate))
    val foobarList3 = GiftList(NotAssigned, Some("A List For Mark"), Some("Something interesting about this list"), Some(dateInFuture.plusDays(7).toDate))
    val foobarList4 = GiftList(NotAssigned, Some("A List For Jack"), Some("Something interesting about this list"), Some(dateInFuture.plusDays(600).toDate))

    val foobar1List = GiftList(NotAssigned, Some("A List by Foobar 1"), Some("Something interesting about this list"), Some(dateInFuture.toDate))

    val foobarListC = GiftList.create(foobarList, foobar.get.id.get)
    val foobar1ListC = GiftList.create(foobarList1, foobar.get.id.get)
    GiftList.create(foobarList2, foobar.get.id.get)
    GiftList.create(foobarList3, foobar.get.id.get)
    GiftList.create(foobarList4, foobar.get.id.get)

    GiftList.create(foobar1List, foobar1.get.id.get)

    // create some items for gift lists
    // foobarList
    GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1)), foobarListC.get.giftListId)
    // foobar1List
    GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1)), foobar1ListC.get.giftListId)

  }

  def cleanDb = {
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database.");
        SQL(
          """
            |truncate item, gift_list_role, users, gift_list;
            |ALTER SEQUENCE gift_list_seq RESTART;
            |ALTER SEQUENCE item_seq RESTART;
            |ALTER SEQUENCE users_seq RESTART;
          """.stripMargin).execute()
      }
    }
  }

}