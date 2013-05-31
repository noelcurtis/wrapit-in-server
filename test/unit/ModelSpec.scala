package unit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.{GiftListRole, GiftList, User}
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
        SQL("truncate gift_list_role, users, gift_list").execute()
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

    GiftList.create(foobarList, foobar.get.id.get)
    GiftList.create(foobar1List, foobar1.get.id.get)
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

    "allow get of GiftListRoles" in new WithCleanDb {
      val foundUser = User.find("foobar@gmail.com")
      foundUser match {
        case Some(foundUser) => {
          val giftListRoles = foundUser.getGiftListRoles
          giftListRoles match {
            case Some(giftListRoles) => assert(!giftListRoles.isEmpty)
            case None => failure("GiftListRoles not found when they should be")
          }
        }
        case None => failure("User not found")
      }
    }
  }

  "Gift List Model" should {

    "allow GiftList creation" in new WithCleanDb {
      val testGiftList = GiftList(NotAssigned, Some("BalaBoo"), Some("For Birthday"), Some(new Date()))
      val newList = GiftList.create(testGiftList)
      newList match {
        case Some(newList) => {
          newList.id should_!= (None)
        }
        case None => failure("Gift List Creation failed")
      }
    }

    "allow GiftList creation for User" in new WithCleanDb {
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

  }

  "Gift List Role Model" should {

    "allow GiftListRole creation" in new WithCleanDb {
      val testRole = GiftListRole(5, 5, Some(GiftListRole.Role.getInt(GiftListRole.Role.Contributor)))
      GiftListRole.create(testRole)
      val foundRole = GiftListRole.find(testRole.userId, testRole.giftListId)
      foundRole match {
        case Some(foundRole) => foundRole shouldEqual (testRole)
        case None => failure("No GiftListRole Found")
      }
    }

  }

}
