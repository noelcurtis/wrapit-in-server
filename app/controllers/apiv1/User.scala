package controllers.apiv1

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json

object User extends Controller{

  def create = Action { implicit request =>
    val json = request.body.asJson
    json match {
      case Some(v) => {
        val user = v.validate[models.User] // validate the json
        user.fold(
          valid = { c =>
            //create a user
            val created = models.User.create(c)
            created match {
              case Some(u) => Created(Json.toJson(u)) // successful create
              case None => Ok(Json.obj("error" -> "could not create user"))
            }
          },
          invalid = { e => Ok(Json.obj("error" -> e.toString())) }
        )
      }
      case None => Ok(Json.obj("error" -> "request body must be json"))
    }
  }

  def authenticate = Action { implicit request =>
    val json = request.body.asJson
    json match {
      case Some(v) => {
        val user = v.validate[models.User] // validate the json
        user.fold(
          valid = { c =>
            // authenticate the user
            if (c.email.isDefined && c.password.isDefined) {
              val auth = models.User.authenticate(c.email.get, c.password.get)
              auth match {
                case Some(u) => Ok(Json.toJson(u)) // succesful auth
                case None => Ok(Json.obj("error" -> "invalid credentials"))
              }
            } else {
              Ok(Json.obj("error" -> "invalid credentials"))
            }
          },
          invalid = { e => Ok(Json.obj("error" -> e.toString())) }
        )
      }
      case None => Ok(Json.obj("error" -> "request body must be json"))
    }
  }

}
