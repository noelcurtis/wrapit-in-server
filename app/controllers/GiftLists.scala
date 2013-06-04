package controllers

import play.api.mvc.{Action, Controller}
import models.{User}
import views.html
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import scala.Some

object GiftLists extends Controller with Secured{

  def index = IsAuthenticated { email => _ =>
    val user = User.find(email)
    user match {
      case Some(user) => Ok(html.gift_lists.index())
      case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
    }
  }

  def create = IsAuthenticated{ email => _ =>
    val user = User.find(email)
    user match {
      case Some(user) => Ok(html.gift_lists.create())
      case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
    }
  }

  val createForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "dueDate" -> text,
      "notes" -> text
    )
  )

//  /**
//   * Handle create account submission.
//   */
//  def handlecreate = Action { implicit request =>
//
//  }

}
