import _root_.models.{Item, GiftList, User}
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
    // seed data in dev mode
    if (play.Play.isDev) {
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
    GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1), imgUrl = Some("http://store.storeimages.cdn-apple.com/3423/as-images.apple.com/is/image/AppleInc/HB956?wid=276&hei=153&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1369943390003")), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1), imgUrl = Some("http://teleflora.edgesuite.net/images/products/HW0_477885.jpg")), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1), imgUrl = Some("http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg")), foobarListC.get.giftListId)
    // foobar1List
    GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1)), foobar1ListC.get.giftListId)

  }

  def cleanDb = {
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database fpr dev.");
        SQL(
          """
            |truncate photo_relation, photo, item, gift_list_role, users, gift_list;
            |ALTER SEQUENCE gift_list_seq RESTART;
            |ALTER SEQUENCE item_seq RESTART;
            |ALTER SEQUENCE users_seq RESTART;
            |ALTER SEQUENCE photo_seq RESTART;
          """.stripMargin).execute()
      }
    }
  }

}