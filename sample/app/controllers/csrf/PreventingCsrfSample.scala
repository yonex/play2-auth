package controllers.csrf

import javax.inject.Inject

import controllers.stack.TokenValidateElement
import jp.t2v.lab.play2.auth.{ AuthConfig, AuthElement }
import jp.t2v.lab.play2.auth.sample.{ Account, Role }
import jp.t2v.lab.play2.auth.sample.Role._
import play.api.Environment
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller

class PreventingCsrfSample @Inject() (environment: Environment, cacheApi: CacheApi) extends Controller with TokenValidateElement with AuthElement[Int, Account, Role] {

  val authConfig: AuthConfig[Int, Account, Role] = new AuthConfigImpl(environment, cacheApi)

  def formWithToken = StackAction(AuthorityKey -> NormalUser, IgnoreTokenValidation -> true) { implicit req =>
    Ok(views.html.csrf.formWithToken())
  }

  def formWithoutToken = StackAction(AuthorityKey -> NormalUser, IgnoreTokenValidation -> true) { implicit req =>
    Ok(views.html.csrf.formWithoutToken())
  }

  val form = Form { single("message" -> text) }

  def submitTarget = StackAction(AuthorityKey -> NormalUser) { implicit req =>
    form.bindFromRequest.fold(
      _       => throw new Exception,
      message => Ok(message).as("text/plain")
    )
  }

}
