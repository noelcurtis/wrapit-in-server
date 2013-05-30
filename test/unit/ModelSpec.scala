package unit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.{GiftListRole, GiftList, User}
import anorm.NotAssigned
import java.util.Date
import play.Logger

class ModelSpec extends Specification {

  "User Model" should {

    "User can be created" in new WithApplication {
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

    "GiftList can be created" in new WithApplication {
      val testGiftList = GiftList(NotAssigned, Some("BalaBoo"), Some("For Birthday"), Some(new Date()))
      val newList = GiftList.create(testGiftList)
      newList match {
        case Some(newList) => {
          newList.id should_!= (None)
        }
        case None => failure("Gift List Creation failed")
      }
    }

    "GiftList can be created for User" in new WithApplication {
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

    "GiftListRole can be created" in new WithApplication {
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
