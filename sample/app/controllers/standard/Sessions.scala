package controllers.standard

import jp.t2v.lab.play2.auth.LoginLogout
import jp.t2v.lab.play2.auth.sample.{ Account, Role }
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{ Action, Controller }
import views.html

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Sessions extends Controller {

  val loginLogout = new LoginLogout[Int, Account, Role](new AuthConfigImpl {})

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  def login = Action { implicit request =>
    Ok(html.standard.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    loginLogout.gotoLogoutSucceeded.map(_.flashing(
      "success" -> "You've been logged out"
    ).removingFromSession("rememberme"))
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.standard.login(formWithErrors))),
      user           => loginLogout.gotoLoginSucceeded(user.get.id)
    )
  }

}