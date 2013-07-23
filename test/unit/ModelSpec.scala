package unit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._
import anorm._
import java.util.Date
import play.Logger
import org.specs2.execute.{AsResult, Result}
import scala.Some
import play.api.Play.current
import play.api.db.DB
import org.joda.time.DateTime
import scala.Some
import engine.{Creator, Purchaser}
import play.api.libs.json.{Writes, Json, JsObject, JsValue}


abstract class WithCleanDb extends WithApplication {
  override def around[T: AsResult](t: => T) = super.around {
    //cleanDb // clean the database
    //insert // create some test data
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
    Item.addPhoto(Item.findById(i1.get).get, "http://store.storeimages.cdn-apple.com/3423/as-images.apple.com/is/image/AppleInc/HB956?wid=276&hei=153&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1369943390003")
    Item.addPhoto(Item.findById(i2.get).get, "http://teleflora.edgesuite.net/images/products/HW0_477885.jpg")
    Item.addPhoto(Item.findById(i3.get).get, "http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg")
    Item.addPhoto(Item.findById(i4.get).get, "http://store.storeimages.cdn-apple.com/3423/as-images.apple.com/is/image/AppleInc/HB956?wid=276&hei=153&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1369943390003")
    Item.addPhoto(Item.findById(i5.get).get, "http://teleflora.edgesuite.net/images/products/HW0_477885.jpg")
    Item.addPhoto(Item.findById(i6.get).get, "http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg")
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

class ModelSpec extends Specification {

  "User Model" should {

    "allow User creation" in new WithCleanDb {
      val testUser = User(NotAssigned, Some("foo@bar.com"), Some("foobar"), Some(new Date()))
      val newUser = User.create(testUser)
      newUser match {
        case Some(newUser) => {
          newUser.id should_!= (None)
        }
        case None => failure("User creation failed")
      }
    }

    "allow User creation without parameters" in new WithCleanDb {
      val testUser = User(email=Some("fab@bar.com"), password=Some("blah"), lastSignIn=None)
      val newUser = User.create(testUser)
      newUser match {
        case Some(newUser) => {
          newUser.id should_!= (None)
          newUser.token should_!=(None)
        }
        case None => failure("User creation failed")
      }
    }

    "allow User sign in" in new WithCleanDb {
      val auth = User.authenticate("foobar@gmail.com", "foobar")
      auth match {
        case Some(user) => success
        case None => failure("User could not be authenticated")
      }
    }

    "not allow bad password sign in" in new WithCleanDb {
      val auth = User.authenticate("foobar@gmail.com", "blah")
      auth match {
        case Some(user) => failure("User allowed with bad password")
        case None => success
      }
    }

  }

  "Gift List Model" should {

    "allow User to create a GiftList" in new WithCleanDb {
      val testGiftList = GiftList(NotAssigned, Some("BalaBoo"), Some("For Birthday"), Some(new Date()))
      val testUser = User(NotAssigned, Some("foo1@bar.com"), Some("foobar"), Some(new Date()))
      val newUser = User.create(testUser)

      val giftListRole = GiftList.create(testGiftList, newUser.get.id.get)
      giftListRole match {
        case Some(giftListRole) => {
          Logger.info(giftListRole.toString)
          giftListRole.userId should_!=(null)
          giftListRole.giftListId should_!=(null)
        }
        case None => failure("Gift List Role Creation failed")
      }
    }

    "allow to add an Item" in new WithCleanDb {
      val testGiftList = GiftList(NotAssigned, Some("BalaBoo"), Some("For Birthday"), Some(new Date()))
      val testUser = User(NotAssigned, Some("foo1@bar.com"), Some("foobar"), Some(new Date()))
      val newUser = User.create(testUser)

      val giftListRole = GiftList.create(testGiftList, newUser.get.id.get)
      val id = GiftList.addItem(Item(name = Some("Yellow Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), newUser.get, giftListRole.get.giftListId)
      assert(id != null)
    }

  }

  "Item Model" should {

    "allow to get Items on a GiftList" in new WithCleanDb {
      val items = Item.find(1)
      Logger.info(items.toString())
      assert(items.isEmpty == false)
      assert(items.length == 4)
    }

    "allow to add a Photo to an Item" in new WithCleanDb {
      val items = Item.find(1)
      val firstItem = items(1)
      Item.addPhoto(firstItem, "http://g-ecx.images-amazon.com/images/G/01/kindle/dp/2012/KT/KT-slate-01-sm._V401027115_.jpg")
      Thread.sleep(3000) // might fail if sleep if too short
      firstItem.getPhoto match {
        case Some(photo) => success
        case None => failure("Could not add Photo to an Item.")
      }
    }

    "allow to create an Item with an ItemRelation" in new WithCleanDb {
      val newRelation = Item.createWithRelation(Item(name = Some("Yellow Gift"), needed = Some(1), giftListId = Some(1)), User.find(1).get)
      newRelation match {
        case Some(newRelation) => newRelation.itemId.should_!=(null); newRelation.userId.should_!=(null); newRelation.relationType.should_==(Creator)
        case None => failure("Could not create ItemRelation")
      }
    }
  }

  "Gift List Role Model" should {

    "allow GiftListRole creation" in new WithCleanDb {
      val testRole = GiftListRole.create(5, 5, Some(GiftListRole.Role.getInt(GiftListRole.Role.Contributor)))
      val foundRole = GiftListRole.find(testRole.get.userId, testRole.get.giftListId)
      foundRole match {
        case Some(foundRole) => foundRole shouldEqual (testRole.get)
        case None => failure("No GiftListRole Found")
      }
    }

    "allows GiftListRoles to be found for user" in new WithCleanDb {
      val user = User.find("foobar@gmail.com")
      val roles = GiftListRole.find(user.get.id.get)
      roles.length shouldEqual (5)
      roles(0).getGiftList.get.name.get shouldEqual("A List by Foobar")
    }

    "check empty list returned for non existant user" in new WithCleanDb {
      val roles = GiftListRole.find(123445)
      assert(roles.isEmpty)
    }

  }

  "Comments Model" should {

    val testComment = "Some new comment"
    val testComment1 = "Some other comment"

    "allow Comments creation" in new WithCleanDb {
      val newComment = Comments.create(testComment)
      newComment match {
        case Some(newComment) =>  {
          newComment.id should_!=(null) // asset not null
          newComment.note shouldEqual(testComment)
        }
        case None => failure("Could not create the comment" + newComment.toString)
      }
    }

    "allow CommentRelation creation" in new WithCleanDb {
      val newComment = Comments.create(testComment) // create a comment for FK constraint

      val newCR = CommentRelation(commentId = 1, userId = 1, itemId = 1)
      val createdCR = CommentRelation.create(newCR)
      createdCR match {
        case Some(c) => {
          c.id should_!=(null)
          c.createdAt should_!=(null)
        }
        case None => failure("Could not create the CommentRelation " + createdCR.toString)
      }
    }

    "allow Comments creation for User and Item" in new WithCleanDb {
      val createdCR = Comments.create(1, 1, testComment)
      createdCR match {
        case Some(c) => {
          c.id should_!=(null)
          c.createdAt should_!=(null)
          c.getComment().get.note shouldEqual(testComment)
        }
        case None => failure("Could not create the CommentRelation " + createdCR.toString)
      }
    }

    "allow Find of CommentRelations for an Item" in new WithCleanDb {
      val createdCR1 = Comments.create(1, 1, testComment)
      val createdCR2 = Comments.create(2, 1, testComment1)

      val found = CommentRelation.find(1)
      found.size should_==(2)

      val dt1 = new DateTime(found(0).createdAt)
      val dt2 = new DateTime(found(1).createdAt)

      assert(dt2.isAfter(dt1))
    }

  }

  "ItemRelation Model" should {

    "allow to create and find ItemRelation" in new WithCleanDb {

      val createdIR1 = ItemRelation.create(ItemRelation(21, 21, Purchaser));
      val createdIR2 = ItemRelation.create(ItemRelation(21, 21, Creator));
      ItemRelation.create(ItemRelation(21, 22, Creator)); // some more just for kicks
      ItemRelation.create(ItemRelation(21, 23, Creator));

      createdIR1 match {
        case Some(createdIR1) => createdIR1.relationType.shouldEqual(Purchaser)
        case None => failure("Could not create ItemRelation")
      }

      createdIR2 match {
        case Some(createdIR2) => createdIR2.relationType.shouldEqual(Creator)
        case None => failure("Could not create ItemRelation")
      }
    }

  }


  "PhotoRelation Model" should {

    "allow to find Photos for all GiftLists for a User" in new WithCleanDb {

      val giftListPhotos = PhotoRelation.findAllForUserGiftList(1)
      assert(giftListPhotos.size > 0)

    }

    "allow to render all Photos for all GiftLists for a User in Json" in new WithCleanDb {

      val giftListPhotos = PhotoRelation.findAllForUserGiftList(1)
      import engine.Utils.mapWrites
      val json = Json.toJson(giftListPhotos)
      Logger.info(json.toString())

    }

  }

}
