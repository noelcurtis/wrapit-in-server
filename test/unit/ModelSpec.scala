package unit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.{GiftListRole, GiftList, User, Item}
import anorm._
import java.util.Date
import play.Logger
import org.specs2.execute.{AsResult, Result}
import scala.Some
import play.api.Play.current
import play.api.db.DB


abstract class WithCleanDb extends WithApplication {
  override def around[T: AsResult](t: => T) = super.around {
    cleanDb // clean the database
    createTestData // create some test data
    AsResult(t)
  }

  def cleanDb = {
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database...");
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

  def createTestData = {
    // create some users
    val foobar = User.create(User(NotAssigned, Some("foobar@gmail.com"), Some("foobar"), Some(new Date())))
    val foobar1 = User.create(User(NotAssigned, Some("foobar1@gmail.com"), Some("foobar1"), Some(new Date())))

    // create some gift lists for users
    val foobarList = GiftList(NotAssigned, Some("foobar"), Some("by foobar"), Some(new Date()))
    val foobar1List = GiftList(NotAssigned, Some("foobar1"), Some("by foobar1"), Some(new Date()))

    val foobarListC = GiftList.create(foobarList, foobar.get.id.get)
    val foobar1ListC = GiftList.create(foobar1List, foobar1.get.id.get)

    // create some items for gift lists
    // foobarList
    GiftList.addItem(Item(name = Some("Yellow Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"),needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobarListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobarListC.get.giftListId)
    // foobar1List
    GiftList.addItem(Item(name = Some("Yellow Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Green Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Blue Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobar1ListC.get.giftListId)
    GiftList.addItem(Item(name = Some("Fish Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), foobar1ListC.get.giftListId)
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
      val id = GiftList.addItem(Item(name = Some("Yellow Gift"), url = Some("http://store.apple.com/us/browse/home/shop_mac/family/macbook_pro"), needed = Some(1)), giftListRole.get.giftListId)
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
      roles.length shouldEqual (1)
      roles(0).getGiftList.get.name.get shouldEqual("foobar")
    }

    "check empty list returned for non existant user" in new WithCleanDb {
      val roles = GiftListRole.find(123445)
      assert(roles.isEmpty)
    }

  }

}
