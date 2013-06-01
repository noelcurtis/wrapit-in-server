package controllers

import play.api.mvc.{Action, Controller}
import models.{User}
import views.html
import play.Logger

object GiftLists extends Controller with Secured{

  /**
   * Display the dashboard.
   */
  def index = IsAuthenticated { email => _ =>
    val user = User.find(email)
    user match {
      case Some(user) => Ok(html.gift_lists.index())
      case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
    }
  }

}
