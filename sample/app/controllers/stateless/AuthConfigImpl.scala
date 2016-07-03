package controllers.stateless

import controllers.BaseAuthConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }
import jp.t2v.lab.play2.auth.{ AsyncIdContainer, CookieIdContainer }
import play.api.Environment
import play.api.cache.CacheApi

class AuthConfigImpl(environment: Environment, cacheApi: CacheApi) extends BaseAuthConfig(environment, cacheApi) {

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Messages.main))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Sessions.login))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Sessions.login))

  override lazy val idContainer = AsyncIdContainer(new CookieIdContainer[Id])

}