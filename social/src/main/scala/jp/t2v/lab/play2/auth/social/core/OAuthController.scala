package jp.t2v.lab.play2.auth.social.core

import jp.t2v.lab.play2.auth.{ AuthConfig, OptionalAuthElement }
import play.api.mvc.{ Controller, RequestHeader, Result }

import scala.concurrent.{ ExecutionContext, Future }

abstract class OAuthController[Id, User, Authority, AccessToken] (authConfig: AuthConfig[Id, User, Authority], authenticator: OAuthAuthenticator[AccessToken])
  extends Controller {

  def onOAuthLoginSucceeded(token: AccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

  def onOAuthLinkSucceeded(token: AccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result]

  protected lazy val OAuthExecutionContext: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

}
