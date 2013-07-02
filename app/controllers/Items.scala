package controllers

import play.api.mvc.Controller
import models.{Comments, Item, User, CommentRelation}
import views.html
import play.Logger
import engine.ImageGetter
import play.api.libs.json.Json

object Items extends Controller with Secured {

  def webimages(listId: Long, itemId: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
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
    authToken => implicit request =>
      val user = User.findByToken(authToken)
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

  def show(listId: Long, itemId: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          val item = Item.findById(itemId)
          item match {
            case Some(item) => {
              val comments = CommentRelation.find(item.id.get) // get all the comments
              Ok(views.html.items.show(item, comments, listId))
            }
            case None => Redirect(routes.GiftLists.show(listId)) // go back to the List page
          }

        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

  def addComment() = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          val comment = request.getQueryString("comment");
          // get comment from query
          val itemId = request.getQueryString("itemId"); // get the item from query
          if (itemId.isDefined && comment.isDefined) {
            val user = User.findByToken(authToken)
            val item = Item.findById(itemId.get.toLong)
            if (item.isDefined) {
              val createdCR = Comments.create(user.get.id.get, item.get.id.get, comment.get) // create the comment
              createdCR match {
                case Some(c) => Ok(Json.toJson(Map("status" -> "ok")))
                case None => Ok(Json.toJson(Map("status" -> "error", "message" -> "comment not created")))
              }
            } else {
              Ok(Json.toJson(Map("status" -> "error", "message" -> "item not found")))
            }
          }
          else {
            Ok(Json.toJson(Map("status" -> "error", "message" -> "invalid parameters")))
          }

        }
        case None => Logger.error("No user!"); Ok(Json.toJson(Map("status" -> "error", "message" -> "invalid user")))
      }
  }


  def updatePurchased() = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          val purchased = request.getQueryString("isPurchased"); // get the purchased changed
          val itemId = request.getQueryString("itemId"); // get the item id
          if (purchased.isDefined && itemId.isDefined) {
            val p = if (purchased.get.toBoolean) 1 else 0
            val item = Item.findById(itemId.get.toLong)
            if (item.isDefined) {
              Item.update(item.get.copy(purchased = Some(p))) // update the item
              Ok(Json.toJson(Map("status" -> "ok")))
            } else {
              Ok(Json.toJson(Map("status" -> "error", "message" -> "item not found")))
            }
          } else {
            Ok(Json.toJson(Map("status" -> "error", "message" -> "invalid parameters")))
          }

        }
        case None => Logger.error("No user!"); Ok(Json.toJson(Map("status" -> "error", "message" -> "invalid user")))
      }
  }


}
