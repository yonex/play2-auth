package jp.t2v.lab.play2.auth

import play.api.mvc._
import scala.reflect.{ClassTag, classTag}
import scala.concurrent.{ExecutionContext, Future}

trait AuthConfig[Id, User, Authority] {

  implicit def idTag: ClassTag[Id]

  def sessionTimeoutInSeconds: Int

  def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]]

  def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]

  def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]

  def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result]

  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result]

  def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean]

  lazy val idContainer: AsyncIdContainer[Id] = AsyncIdContainer(new CacheIdContainer[Id])

  @deprecated("it will be deleted since 0.14.x. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val cookieName: String = throw new AssertionError("use tokenAccessor setting instead.")

  @deprecated("it will be deleted since 0.14.0. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val cookieSecureOption: Boolean = throw new AssertionError("use tokenAccessor setting instead.")

  @deprecated("it will be deleted since 0.14.0. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val cookieHttpOnlyOption: Boolean = throw new AssertionError("use tokenAccessor setting instead.")

  @deprecated("it will be deleted since 0.14.0. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val cookieDomainOption: Option[String] = throw new AssertionError("use tokenAccessor setting instead.")

  @deprecated("it will be deleted since 0.14.0. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val cookiePathOption: String = throw new AssertionError("use tokenAccessor setting instead.")

  @deprecated("it will be deleted since 0.14.0. use CookieTokenAccessor constructor", since = "0.13.1")
  final lazy val isTransientCookie: Boolean = throw new AssertionError("use tokenAccessor setting instead.")

  lazy val tokenAccessor: TokenAccessor = new CookieTokenAccessor(
    cookieName = "PLAY2AUTH_SESS_ID",
    cookieSecureOption = play.api.Play.maybeApplication.exists(app => play.api.Play.isProd(app)),
    cookieHttpOnlyOption = true,
    cookieDomainOption = None,
    cookiePathOption = "/",
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

  def authorized(authority: Authority)(implicit request: RequestHeader, context: ExecutionContext): Future[Either[Result, (User, ResultUpdater)]] = {
    restoreUser collect {
      case (Some(user), resultUpdater) => Right(user -> resultUpdater)
    } recoverWith {
      case _ => authenticationFailed(request).map(Left.apply)
    } flatMap {
      case Right((user, resultUpdater)) => authorize(user, authority) collect {
        case true => Right(user -> resultUpdater)
      } recoverWith {
        case _ => authorizationFailed(request, user, Some(authority)).map(Left.apply)
      }
      case Left(result) => Future.successful(Left(result))
    }
  }

  private[auth] def restoreUser(implicit request: RequestHeader, context: ExecutionContext): Future[(Option[User], ResultUpdater)] = {
    (for {
      token  <- extractToken(request)
    } yield for {
      Some(userId) <- idContainer.get(token)
      Some(user)   <- resolveUser(userId)
      _            <- idContainer.prolongTimeout(token, sessionTimeoutInSeconds)
    } yield {
      Option(user) -> tokenAccessor.put(token) _
    }) getOrElse {
      Future.successful(Option.empty -> identity)
    }
  }

  private[auth] def extractToken(request: RequestHeader): Option[AuthenticityToken] = {
    if (play.api.Play.maybeApplication.forall(app => play.api.Play.isTest(app))) {
      request.headers.get("PLAY2_AUTH_TEST_TOKEN") orElse tokenAccessor.extract(request)
    } else {
      tokenAccessor.extract(request)
    }
  }

}
