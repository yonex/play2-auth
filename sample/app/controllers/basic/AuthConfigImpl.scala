package controllers.basic

import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.{ ExecutionContext, Future }
import jp.t2v.lab.play2.auth.AuthConfig
import jp.t2v.lab.play2.auth.sample.{ Account, Role }
import jp.t2v.lab.play2.auth.sample.Role._
import play.api.Environment
import play.api.cache.CacheApi

import scala.reflect.{ ClassTag, classTag }

class AuthConfigImpl(environment: Environment, cacheApi: CacheApi) extends AuthConfig[Account, Account, Role](environment, cacheApi) {

  type Id = Account
  type User = Account
  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext) = Future.successful(Some(id))
  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext) = Future.successful((user.role, authority) match {
    case (Administrator, _) => true
    case (NormalUser, NormalUser) => true
    case _ => false
  })

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = throw new AssertionError("don't use application Login")
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = throw new AssertionError("don't use application Logout")
  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful {
    Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="SECRET AREA"""")
  }
  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext) = Future.successful(Forbidden("no permission"))

  override lazy val idContainer = new BasicAuthIdContainer

  override lazy val tokenAccessor = new BasicAuthTokenAccessor

}