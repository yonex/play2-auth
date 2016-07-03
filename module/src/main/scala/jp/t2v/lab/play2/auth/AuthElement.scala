package jp.t2v.lab.play2.auth

import play.api.mvc.{Result, Controller}
import jp.t2v.lab.play2.stackc.{RequestWithAttributes, RequestAttributeKey, StackableController}
import scala.concurrent.Future

trait AuthElement[Id, User, Authority] extends StackableController {
    self: Controller =>

  val authConfig: AuthConfig[Id, User, Authority]

  private[auth] case object AuthKey extends RequestAttributeKey[User]
  case object AuthorityKey extends RequestAttributeKey[Authority]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    implicit val (r, ctx) = (req, StackActionExecutionContext(req))
    req.get(AuthorityKey) map { authority =>
      authConfig.authorized(authority) flatMap {
        case Right((user, resultUpdater)) => super.proceed(req.set(AuthKey, user))(f).map(resultUpdater)
        case Left(result)                 => Future.successful(result)
      }
    } getOrElse {
      authConfig.restoreUser collect {
        case (Some(user), _) => user
      } flatMap {
        authConfig.authorizationFailed(req, _, None)
      } recoverWith {
        case _ => authConfig.authenticationFailed(req)
      }
    }
  }

  implicit def loggedIn(implicit req: RequestWithAttributes[_]): User = req.get(AuthKey).get

}

trait OptionalAuthElement[Id, User, Authority] extends StackableController {
    self: Controller =>

  val authConfig: AuthConfig[Id, User, Authority]

  private[auth] case object AuthKey extends RequestAttributeKey[User]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    implicit val (r, ctx) = (req, StackActionExecutionContext(req))
    val maybeUserFuture = authConfig.restoreUser.recover { case _ => None -> identity[Result] _ }
    maybeUserFuture.flatMap { case (maybeUser, cookieUpdater) =>
      super.proceed(maybeUser.map(u => req.set(AuthKey, u)).getOrElse(req))(f).map(cookieUpdater)
    }
  }

  implicit def loggedIn[A](implicit req: RequestWithAttributes[A]): Option[User] = req.get(AuthKey)
}

trait AuthenticationElement[Id, User, Authority] extends StackableController {
    self: Controller =>

  val authConfig: AuthConfig[Id, User, Authority]

  private[auth] case object AuthKey extends RequestAttributeKey[User]

  override def proceed[A](req: RequestWithAttributes[A])(f: RequestWithAttributes[A] => Future[Result]): Future[Result] = {
    implicit val (r, ctx) = (req, StackActionExecutionContext(req))
    authConfig.restoreUser recover {
      case _ => None -> identity[Result] _
    } flatMap {
      case (Some(u), cookieUpdater) => super.proceed(req.set(AuthKey, u))(f).map(cookieUpdater)
      case (None, _)                => authConfig.authenticationFailed(req)
    }
  }

  implicit def loggedIn(implicit req: RequestWithAttributes[_]): User = req.get(AuthKey).get

}
