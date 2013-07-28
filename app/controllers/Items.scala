package controllers

import models._
import play.Logger
import engine.{Utils, Purchaser, ImageGetter}
import play.api.libs.json.Json
import scala.Some
import play.api.mvc._
import com.google.common.io.Files
import java.io.File

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
          val itemRelation = ItemRelation.find(itemId, Purchaser) // find ItemRelation of Purchaser
          val item = Item.findById(itemId)
          item match {
            case Some(item) => {
              val comments = CommentRelation.find(item.id.get) // get all the comments
              Logger.info(item.toString)
              Ok(views.html.items.show(item, itemRelation, comments, listId, user))
            }
            case None => Redirect(routes.GiftLists.show(listId)) // go back to the List page
          }

        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }

  def addComment(listId: Long, itemId: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          val formData = request.body.asFormUrlEncoded
          // get comment from query
          if (formData.isDefined && formData.get.get("comment").isDefined) {
            val item = Item.findById(itemId)
            if (item.isDefined) {
              val c = formData.get.get("comment").get.head
              val createdCR = Comments.create(user.id.get, itemId, c) // create the comment
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


  def updatePurchased(listId: Long, itemId: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {

          val formData = request.body.asFormUrlEncoded
          if (formData.isDefined && formData.get.get("purchased").isDefined) {
            val p = formData.get.get("purchased").get.head.toInt
            val item = Item.findById(itemId)
            if (item.isDefined) {

              val relation = ItemRelation.find(user.id.get, itemId, Purchaser) // find a Purchaser relation
              Item.update(item.get.copy(purchased = Some(p))) // update the item
              if (p == 1 && item.get.purchased.getOrElse(0) < 1)
                ItemRelation.create(ItemRelation(user.id.get, itemId, Purchaser)) // create an ItemRelation
              else {
                relation match {
                  // delete the ItemRelation only if this user owns the ItemRelation
                  case Some(relation) => ItemRelation.delete(user.id.get, itemId, Purchaser)
                  case None => Logger.info("User " + user.id + " does not own Purchaser relation on item " + item.get.id)
                }
              }
              Ok(Json.toJson(Map("status" -> "ok", "purchased" -> p.toString)))

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


  def addPhoto(listId: Long, itemId: Long) = IsAuthenticated {
    authToken => implicit request =>
      val user = User.findByToken(authToken)
      user match {
        case Some(user) => {
          Ok(views.html.items.photoupload(listId, itemId))
        }
        case None => Logger.error("No user!"); Redirect(routes.Application.index).withNewSession
      }
  }


  def uploadPhoto(listId: Long, itemId: Long) = Action(parse.multipartFormData) { request =>
    val foundItem = Item.findById(itemId)
    foundItem match {
      case Some(item) => {
        request.body.file("picture").map { picture =>
          try {

            val filename = picture.filename // get the file name
            val contentType = picture.contentType // get the content type
            val tmpFile = new File(Utils.getTempFilePath(filename)) // get temp file path
            picture.ref.moveTo(tmpFile) // move to temp file
            val smallerFilePath = Utils.resizeImage(tmpFile) // resize the file
            smallerFilePath match {
              case Some(s) => {
                val asBytes = Files.toByteArray(new File(s)) // convert to byte array
                Item.addPhoto(item, asBytes, contentType) // upload s3 and add the photo to the Item
              }
              case None => Logger.error("Not Image to push to S3")
            }

          } catch {
            case e:Exception => Logger.error(e.getMessage)
          }
          Redirect(routes.Items.show(listId, itemId)) // redirect to the item page once added
        }.getOrElse {
          Redirect(routes.GiftLists.show(listId)).flashing(
            "error" -> "Missing file"
          )
        }
      }
      case None => Logger.error("Item not found: " + itemId); Redirect(routes.GiftLists.show(listId)) // go back to the List page
    }

  }

}
