package controllers

import play.api.mvc.{Action, Controller}
import models.{GiftListRole, User, GiftList}
import anorm.NotAssigned
import java.util.Date
import play.Logger


object Test extends Controller {

  val testUser = User(NotAssigned, Some("foo@bar.com"), Some("PWD"), Some(new Date()))
  val testGiftList = GiftList(NotAssigned, Some("BalaBoo"), Some("For Birthday"), Some(new Date()))
  val testRole = GiftListRole(5, 5, Some(GiftListRole.Role.getInt(GiftListRole.Role.Contributor)))

  def testModel = Action {
    Ok
  }

  def testUserCreate = Action {
    val newUser = User.create(testUser)
    newUser match {
      case Some(newUser) => {
        val foundUser = User.find(newUser.id.get)
        Logger.info(foundUser.toString)
      }
      case None => Logger.error("No User Found")
    }
    Ok
  }

  def testGiftListCreate = Action {
    val newList = GiftList.create(testGiftList)
    newList match {
      case Some(newList) => {
        val foundList = GiftList.find(newList.id.get)
        Logger.info(foundList.toString)
      }
      case None => Logger.error("No Gift List Found")
    }
    Ok
  }

  def testGiftListRole = Action {
    GiftListRole.create(testRole)
    val foundRole = GiftListRole.find(testRole.userId, testRole.giftListId)
    foundRole match {
      case Some(foundRole) => Logger.info(foundRole.toString)
      case None => Logger.error("Could not find Role")
    }
    Ok
  }

}
