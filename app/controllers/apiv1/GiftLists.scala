package controllers.apiv1

import play.api.mvc.Controller
import controllers.Authenticated
import play.api.libs.json.Json
import models.{GiftList, PhotoRelation, GiftListRole}
import engine.Utils.mapWrites

object GiftLists extends Controller with Authenticated{

  def index = isAuthenticated {
    u => _ => {
      u match {
        case Some (user) => {
          val giftListRoles = GiftListRole.find(user.id.get)
          Ok(Json.toJson(giftListRoles))
        }
        case None => Ok(Json.obj("error" -> "no user"))
      }
    }
  }

  def photos = isAuthenticated {
    u => _ => {
      u match {
        case Some (user) => {
          val photosJson = Json.toJson(PhotoRelation.findAllForUserGiftList(user.id.get))
          Ok(photosJson)
        }
        case None => Ok(Json.obj("error" -> "no user"))
      }
    }
  }

  def create = isAuthenticated {
    u => implicit request => {
      u match {
        case Some (user) => {
          val json = request.body.asJson
          val giftList = json.get.validate[GiftList] // get the gift list from JSON
          giftList.fold(
            valid = { c =>
              // create a new role
              val giftListRole = GiftList.create(c, user.id.get)

              giftListRole match {
                case Some(u) => Created(Json.toJson(u)) // successful create
                case None => Ok(Json.obj("error" -> "could not create gift list"))
              }
            },
            invalid = { e => Ok(Json.obj("error" -> e.toString())) }
          )
        }
        case None => Ok(Json.obj("error" -> "no user"))
      }
    }
  }

}
