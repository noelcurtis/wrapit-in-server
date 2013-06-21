package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.User
import views.html

object Application extends Controller {

  /**
   * Login form
   */
  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
    })
  )

  /**
   * create form
   *
   */
  val createForm = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => User.create(User(email = Some(email), password = Some(password))).isDefined
    })
  )

  def index = Action {
    implicit request =>
      val authToken = request.session.get("authtoken")
      authToken match {
        case Some(authToken) => Redirect(routes.GiftLists.index) // if already authenticated
        case None => Ok(views.html.index(loginForm)) // if not authenticated
      }
  }

  def create = Action {
    implicit request =>
      val authToken = request.session.get("authtoken")
      authToken match {
        case Some(authToken) => Redirect(routes.GiftLists.index)
        case None => Ok(views.html.create(createForm))
      }
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.index(formWithErrors)),
        user => {
          val foundUser = User.find(user._1)
          try {
            Redirect(routes.GiftLists.index).withSession("authtoken" -> foundUser.get.token.get)
          } catch {
            case e:Exception => play.Logger.error("Could not auth User " + e.getMessage); Redirect(routes.Application.index)
          }
        }
      )
  }

  /**
   * Handle create account submission.
   */
  def handlecreate = Action {
    implicit request =>
      createForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.create(formWithErrors)),
        user => {
          val foundUser = User.find(user._1)
          try {
            Redirect(routes.GiftLists.index).withSession("authtoken" -> foundUser.get.token.get)
          } catch {
            case e:Exception => play.Logger.error("Could not created User " + e.getMessage); Redirect(routes.Application.index)
          }
        }
      )
  }

}


/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user authtoken.
   */
  private def authToken(request: RequestHeader) = request.session.get("authtoken")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index())

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(authToken, onUnauthorized) {
    user =>
      Action(request => f(user)(request))
  }

}