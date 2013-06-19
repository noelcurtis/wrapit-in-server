package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.ws.WS
import play.api.Play
import play.api.Play.current
import engine.Utils;
import play.Logger;
import scala.concurrent.{Await, ExecutionContext}
import ExecutionContext.Implicits.global
import models.{FbInfo, User}
import scala.concurrent.duration.FiniteDuration
import org.joda.time.DateTime
import play.api.libs.json.Json

object FacebookAuth extends Controller {

  //graph.facebook.com/oauth/access_token?client_id=346561915470020&client_secret=3d3247ec4e7e1b7983db74dc225bf994&grant_type=client_credentials

  var accessToken = "346561915470020|IM2K8lxrWyEQtYP9eT2dJj6l-SU"
  var clientId = "346561915470020"
  var clientSecret = "3d3247ec4e7e1b7983db74dc225bf994"
  var facebookAuthUrl = "https://www.facebook.com/dialog/oauth"
  var redirectBase = if (play.Play.application().isDev) "http://0.0.0.0:9000" else "http://wrapitin.heroku.com"
  var redirectAccessToken = "https://graph.facebook.com/oauth/access_token"
  var debugToken = "https://graph.facebook.com/debug_token"

  def facebookAuthenticate = Action {
    Logger.info("Authenticating with Facebook")
    Redirect(url = facebookAuthUrl, queryString = Map(
      "client_id" -> Seq(clientId),
      "redirect_uri" -> Seq(redirectBase + routes.FacebookAuth.facebookRedirect().url),
      "scope" -> Seq("email", "publish_actions") //TODO: Add sate query string
    )
    )
  }

  def facebookRedirect = Action {
    implicit request => {
      Logger.info("Facebook redirect")
      if (request.queryString.get("code").isDefined) {
        Logger.info("Facebook requesting access token")
        Async {
          WS.url(redirectAccessToken).withQueryString(
            ("client_id", clientId),
            ("redirect_uri", redirectBase + routes.FacebookAuth.facebookRedirect().url),
            ("client_secret", clientSecret),
            ("code", request.queryString.get("code").get.head)
          ).get().map {
            response => {
              Logger.info("Facebook getting token response")
              val body = response.getAHCResponse.getResponseBody
              val pattern = "access_token=(\\S*)&expires=(\\d*)".r
              body match {
                case pattern(token, expires) => {
                  val timeleft = Utils.toInt(expires, 0)
                  Logger.info("Token " + token + " Expires " + timeleft)
                  // verify the token
                  val facebookUserId = fbDebugToken(token)
                  Ok
                }
                case _ => Logger.info("Facebook error " + body.toString); Redirect(routes.Application.index)
              }
            }
          }
        }
      } else {
        Logger.info("Facebook error " + request.queryString.toString)
        Redirect(routes.Application.index)
      }
    }
  }


  def fbDebugToken(token: String): Option[Long] = {
    Logger.info("Facebook verifying response")
    try {
      val future = WS.url(debugToken).withQueryString( ("input_token", token), ("access_token", accessToken)).get()
      val response = Await.result(future, FiniteDuration(10, "seconds")) // wait a max of 10 seconds
      val json = Json.parse(response.getAHCResponse.getResponseBody) // response body as JSON
      val userId = (json \ "data" \ "user_id").asOpt[Long]
      val appId = (json \ "data" \ "app_id").asOpt[Long]
      if (userId.isDefined && appId.isDefined) {
        Logger.info("Facebook verified token response " + response.getAHCResponse.getResponseBody)
        userId
      } else {
        Logger.info("Facebook verifying token error " + response.getAHCResponse.getResponseBody)
        None
      }
    } catch {
      case e: Exception => "Facebook could not verify token " + e.getMessage; None
    }
  }


}
