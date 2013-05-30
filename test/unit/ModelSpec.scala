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
    DB.withConnection {
      implicit connection => {
        Logger.debug("Clearing database...");
        SQL("truncate gift_list_role, users, gift_list").execute()
      }
    }
    AsResult(t)
  }
}

class ModelSpec extends Specification {

  "User Model" should {

    "User can be created" in new WithCleanDb {
      val testUser = User(NotAssigned, Some("foo@bar.com"), Some("foobar"), Some(new Date()))
      val newUser = User.create(testUser)
      newUser match {
        case Some(newUser) => {
          newUser.id should_!= (None)
        }
        case None => failure("User creation failed")
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
