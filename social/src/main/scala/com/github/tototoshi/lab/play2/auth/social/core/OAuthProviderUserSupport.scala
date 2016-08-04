package com.github.tototoshi.play2.auth.social.core

import scala.concurrent.{ ExecutionContext, Future }

trait OAuthProviderUserSupport {
    self: OAuthController =>

  type ProviderUser

  def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser]

}
