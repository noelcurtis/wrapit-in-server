package controllers

import play.api.mvc.{Action, Controller}
import models.{GiftListRole, GiftList, User}
import views.html
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import java.text.SimpleDateFormat

object GiftLists extends Controller with Secured{

  def index = IsAuthenticated { email => _ =>
    val user = User.find(email)
    user match {
      case Some(user) => Ok(html.gift_lists.index(GiftListRole.find(user.id.get)))
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

  def create = IsAuthenticated { email => implicit request =>
    val user = User.find(email)
    user match {
      case Some(user) => Ok(html.gift_lists.create(createForm))
      case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
    }
  }

  def handlecreate = IsAuthenticated { email => implicit request =>
    // consider email will always exist
    createForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.gift_lists.create(formWithErrors))
      },
      giftList => {
        val user = User.find(email)
        // parse the date
        val newList = GiftList(name = Some(giftList._1), dueDate = engine.Utils.dateFromString(Some(giftList._2)), purpose = Some(giftList._3))
        // create a new list
        GiftList.create(newList, user.get.id.get)
        Redirect(routes.GiftLists.index())
      }
    )
  }

}
