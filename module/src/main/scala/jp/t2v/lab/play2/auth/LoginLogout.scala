package jp.t2v.lab.play2.auth

import play.api.mvc._
import play.api.mvc.Cookie
import play.api.libs.Crypto
import scala.concurrent.{Future, ExecutionContext}

class Login[Id, User, Authority](val authConfig: AuthConfig[Id, User, Authority]) {

  def gotoLoginSucceeded(userId: Id)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    gotoLoginSucceeded(userId, authConfig.loginSucceeded(request))
  }

  def gotoLoginSucceeded(userId: Id, result: => Future[Result])(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = for {
    token <- authConfig.idContainer.startNewSession(userId, authConfig.sessionTimeoutInSeconds)
    r     <- result
  } yield authConfig.tokenAccessor.put(token)(r)
}

class Logout[Id, User, Authority](val authConfig: AuthConfig[Id, User, Authority]) {
  def gotoLogoutSucceeded(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    gotoLogoutSucceeded(authConfig.logoutSucceeded(request))
  }

  def gotoLogoutSucceeded(result: => Future[Result])(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    authConfig.tokenAccessor.extract(request) foreach authConfig.idContainer.remove
    result.map(authConfig.tokenAccessor.delete)
  }
}

class LoginLogout[Id, User, Authority](val authConfig: AuthConfig[Id, User, Authority]) {

  val login = new Login[Id, User, Authority](authConfig)

  val logout = new Logout[Id, User, Authority](authConfig)

  def gotoLoginSucceeded(userId: Id)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] =
    login.gotoLoginSucceeded(userId)(request, ctx)

  def gotoLoginSucceeded(userId: Id, result: => Future[Result])(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] =
    login.gotoLoginSucceeded(userId, result)(request, ctx)

  def gotoLogoutSucceeded(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] =
    logout.gotoLogoutSucceeded(request, ctx)

  def gotoLogoutSucceeded(result: => Future[Result])(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] =
    logout.gotoLogoutSucceeded(result)(request, ctx)

}