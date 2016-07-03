package controllers.ephemeral

import controllers.BaseAuthConfig
import jp.t2v.lab.play2.auth.{ CookieTokenAccessor, TokenAccessor }
import play.api.Environment
import play.api.cache.CacheApi
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }

class AuthConfigImpl(environment: Environment, cacheApi: CacheApi) extends BaseAuthConfig(environment, cacheApi) {

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Messages.main))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Sessions.login))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Sessions.login))

  override lazy val tokenAccessor: TokenAccessor = new CookieTokenAccessor(
    cookieName = "PLAY2AUTH_SESS_ID",
    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieHttpOnlyOption = true,
    cookieDomainOption = None,
    cookiePathOption = "/",
    cookieMaxAge = None
  )


}