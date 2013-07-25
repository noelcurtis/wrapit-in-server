package apiv1.integration

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS
import play.api.libs.json.{JsResult, Json}
import models.{Item, GiftList, User}
import org.specs2.execute.AsResult
import play.Logger
import anorm._
import java.util.Date
import org.joda.time.DateTime
import play.api.db.DB
import scala.Some

abstract class WithCleanDb extends WithServer {
  override def around[T: AsResult](t: => T) = super.around {
    cleanDb // clean the database
    insert // create some test data
    AsResult(t)
  }

  def insert = {
    Logger.debug("Creating test data.");
    // create some users
    val foobar = User.create(User(NotAssigned, Some("foobar@gmail.com"), Some("foobar"), Some(new Date())), Some("a"))
    val foobar1 = User.create(User(NotAssigned, Some("foobar1@gmail.com"), Some("foobar1"), Some(new Date())), Some("b"))

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
    val i1 = GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1)), foobar.get, foobarListC.get.giftListId)
    val i2 = GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobar.get, foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1)), foobar.get, foobarListC.get.giftListId)
    val i3 = GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1)), foobar.get, foobarListC.get.giftListId)

    // foobar1List
    val i4 = GiftList.addItem(Item(name = Some("Yellow Gift"), needed = Some(1)), foobar.get, foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), needed = Some(1)), foobar.get, foobar1ListC.get.giftListId)
    val i5 = GiftList.addItem(Item(name = Some("Blue Gift"), needed = Some(1)), foobar.get, foobar1ListC.get.giftListId)
    val i6 = GiftList.addItem(Item(name = Some("Fish Gift"), needed = Some(1)), foobar.get, foobar1ListC.get.giftListId)

    // Add photos to the items Expect Items.
    Item.addPhoto(Item.findById(i1.get).get, "http://store.storeimages.cdn-apple.com/3423/as-images.apple.com/is/image/AppleInc/HB956?wid=276&hei=153&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1369943390003", false)
    Item.addPhoto(Item.findById(i2.get).get, "http://teleflora.edgesuite.net/images/products/HW0_477885.jpg",false )
    Item.addPhoto(Item.findById(i3.get).get, "http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg", false)
    Item.addPhoto(Item.findById(i4.get).get, "http://store.storeimages.cdn-apple.com/3423/as-images.apple.com/is/image/AppleInc/HB956?wid=276&hei=153&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1369943390003", false)
    Item.addPhoto(Item.findById(i5.get).get, "http://teleflora.edgesuite.net/images/products/HW0_477885.jpg", false)
    Item.addPhoto(Item.findById(i6.get).get, "http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg", false)
  }

  def cleanDb = {
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database for test.");
        SQL(
          """
            |truncate user_item_relation, comments, photo_relation, photo, comment_relation, item, gift_list_role, fb_info, users, gift_list;
            |ALTER SEQUENCE gift_list_seq RESTART;
            |ALTER SEQUENCE item_seq RESTART;
            |ALTER SEQUENCE users_seq RESTART;
            |ALTER SEQUENCE photo_seq RESTART;
            |ALTER SEQUENCE comments_seq RESTART;
            |ALTER SEQUENCE comment_relation_seq RESTART;
          """.stripMargin).execute()
      }
    }
  }

}

class UserSpec extends Specification{

  val endpoint = "http://localhost:"+ testServerPort +"/apiv1/"

  val user1 = Json.obj(
    "email" -> "foobarapi@gmail.com",
    "password" -> "foobar"
  )

  val user2 = Json.obj(
    "email" -> "foobar@gmail.com",
    "password" -> "foobar"
  )

  "API User" should {

    "Allow to create new User" in new WithCleanDb {

      val request = WS.url(endpoint + "user").post(user1)

      val result = await(request)
      result.status shouldEqual(CREATED)

      val createdUser = Json.parse(result.getAHCResponse.getResponseBody)

      val res: JsResult[User] = createdUser.validate[User]
      res.fold(
        valid = { c => c.email shouldEqual(Some("foobarapi@gmail.com")) },
        invalid = { e => println( e ); failure("Could not parse User from JSON") }
      )

    }

    "Allow User to authenticate" in new WithCleanDb {

      val request = WS.url(endpoint + "authenticate").post(user2)

      val result = await(request)
      result.status shouldEqual(OK)

      val authUser = Json.parse(result.getAHCResponse.getResponseBody)

      val res: JsResult[User] = authUser.validate[User]
      res.fold(
        valid = { c => c.token shouldEqual(Some("a")) },
        invalid = {
          e => println( e )
            failure("Could not parse User from JSON")
            println(result.getAHCResponse.getResponseBody)
        }
      )

    }

  }

}
