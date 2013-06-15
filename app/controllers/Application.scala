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
      "email" -> text,
      "password" -> text
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
      "email" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => User.create(User(email = Some(email), password = Some(password))).isDefined
    })
  )

  def index = Action {
    implicit request =>
      val email = request.session.get("email")
      email match {
        case Some(email) => Redirect(routes.GiftLists.index)
        case None => Ok(views.html.index(loginForm))
      }
  }

  def create = Action {
    implicit request =>
      val email = request.session.get("email")
      email match {
        case Some(email) => Redirect(routes.GiftLists.index)
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
        user => Redirect(routes.GiftLists.index).withSession("email" -> user._1)
      )
  }

  /**
   * Handle create account submission.
   */
  def handlecreate = Action {
    implicit request =>
      createForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.create(formWithErrors)),
        user => Redirect(routes.GiftLists.index).withSession("email" -> user._1)
      )
  }

}


/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index())

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
    user =>
      Action(request => f(user)(request))
  }

}