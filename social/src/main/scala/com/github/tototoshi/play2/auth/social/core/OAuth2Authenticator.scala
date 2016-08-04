package com.github.tototoshi.play2.auth.social.core

import play.api.libs.ws.WSResponse
import play.api.mvc.{ AnyContent, Request }

import scala.concurrent.{ ExecutionContext, Future }

trait OAuth2Authenticator extends OAuthAuthenticator {

  val providerName: String

  val callbackUrl: String

  val accessTokenUrl: String

  val authorizationUrl: String

  val clientId: String

  val clientSecret: String

  def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken]

  def getAuthorizationUrl(request: Request[AnyContent], state: String): String

  def parseAccessTokenResponse(response: WSResponse): String

}
