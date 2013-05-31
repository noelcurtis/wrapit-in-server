package controllers

import play.api.mvc.{Action, Controller}
import models.{GiftListRole, User, GiftList}
import anorm.NotAssigned
import java.util.Date
import play.Logger


object Test extends Controller {

  def testroles = Action {
    GiftListRole.find(56)
    Ok
  }

}
