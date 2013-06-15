package controllers

import play.api.mvc.Controller
import models.{Item, User}
import views.html
import play.Logger
import engine.ImageGetter

object Items extends Controller with Secured {

  def webimages(listId: Long, itemId: Long) = IsAuthenticated {
    email => implicit request =>
      val user = User.find(email)
      user match {
        case Some(user) => {
          val foundItem = Item.findById(itemId)
          foundItem match {
            case Some(foundItem) => {
              val images = ImageGetter.getImages(foundItem.url)
              Ok(views.html.items.webimages(images, foundItem, listId))
            }
            case None => Logger.error(s"Could not find item with id {$itemId}"); Redirect(routes.Application.index)
          }
        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }


  def update(listId: Long, itemId: Long) = IsAuthenticated {
    email => implicit request =>
      val user = User.find(email)
      user match {
        case Some(user) => {
          val url = request.getQueryString("imgUrl")
          val item = Item.findById(itemId)
          item match {
            case Some(item) => Item.addPhoto(item, url.getOrElse(""))
            case None => Logger.error(s"Item not found id ${itemId}")
          }
          Redirect(routes.GiftLists.show(listId))
        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

}
