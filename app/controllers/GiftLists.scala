package controllers

import play.api.mvc.Controller
import models._
import views.html
import play.Logger
import play.api.data.Form
import play.api.data.Forms._
import org.joda.time.DateTime
import play.api.libs.json.Json
import scala.Some
import engine.Utils.mapWrites

object GiftLists extends Controller with Secured {

  def index = IsAuthenticated {
    authToken => _ =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {
          val photosJson = Json.toJson(PhotoRelation.findAllForUserGiftList(user.id.get))
          val giftListRole = GiftListRole.find(user.id.get)
          Ok(html.gift_lists.index(giftListRole, photosJson))
        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

  val createGiftForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "dueDate" -> jodaDate("MM/dd/yyyy"),
      "notes" -> text
    ).verifying(
      // Add an additional constraint: dueDate must be after today
      "Date can be today or some future day.", formValues => !formValues._2.isBefore(new DateTime().minusHours(24))
    )
  )

  def create = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => Ok(html.gift_lists.create(createGiftForm))
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

  def handlecreate = IsAuthenticated {
    authToken => implicit request =>
    // consider email will always exist
      createGiftForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.info("Create GiftList form error " + formWithErrors.errorsAsJson.toString())
          BadRequest(html.gift_lists.create(formWithErrors))
        },
        giftList => {
          val user = User.findByToken(authToken)
          // parse the date
          val newList = GiftList(name = Some(giftList._1), dueDate = Some(giftList._2.toDate), purpose = Some(giftList._3))
          // create a new list
          val giftListRole = GiftList.create(newList, user.get.id.get)
          giftListRole match {
            case Some(giftListRole) => Redirect(routes.GiftLists.show(giftListRole.giftListId))
            case None => Redirect(routes.GiftLists.index())
          }
        }
      )
  }

  def show(id: Long) = IsAuthenticated {
    authToken => implicit request =>
      val list = GiftList.find(id)
      val items = Item.find(id)
      Ok(views.html.gift_lists.show(list, items))
  }

  val itemCreateForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "needed" -> number,
      "link" -> text,
      "getImage" -> number
    ).verifying(
      // Add an additional constraint: link should be a valid URL
    )
  )

  def additem(id: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => Ok(html.items.create(itemCreateForm, id))
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

  def handleadditem(listId: Long) = IsAuthenticated {
    authToken => implicit request => {
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          // Bind the form and create the item
          itemCreateForm.bindFromRequest().fold(
            formWithErrors => {
              BadRequest(html.items.create(formWithErrors, listId))
            },
            item => {
              // parse the date
              val newItem = Item(name = Some(item._1), needed = Some(item._2), url = Some(item._3))
              // add item to the list
              val newItemId = GiftList.addItem(newItem, user, listId)
              newItemId match {
                case Some(newItemId) => {
                  if (item._4 == 1) {
                    Redirect(routes.Items.webimages(listId, newItemId)) // Get images from the web
                  }
                  else {
                    Redirect(routes.GiftLists.show(listId)) // Upload an image from your device
                  }
                }
                case None => Logger.error("Could not add Item " + newItem.toString + " to GiftList " + listId); Redirect(routes.GiftLists.show(listId))
              }
            }
          )

        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
    }
  }

}
