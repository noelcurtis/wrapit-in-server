package controllers.apiv1

import play.api.mvc.{Action, Controller}
import engine.Utils
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
              case Some(u) => Ok(Json.toJson(u)) // successful create
              case None => Ok(Json.obj("error" -> "Could not create User"))
            }
          },
          invalid = { e => Ok(Json.obj("error" -> e.toString())) }
        )
      }
      case None => Ok(Json.obj("error" -> "Request body must by JSON"))
    }
  }

}
