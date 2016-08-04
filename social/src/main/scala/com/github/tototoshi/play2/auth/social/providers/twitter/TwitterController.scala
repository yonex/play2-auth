package com.github.tototoshi.play2.auth.social.providers.twitter

import com.github.tototoshi.play2.auth.social.core.OAuth10aController
import com.github.tototoshi.play2.auth.{ AuthConfig, Login, OptionalAuthElement }
import play.api.libs.oauth.RequestToken

trait TwitterController extends OAuth10aController
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new TwitterAuthenticator

  def requestTokenToAccessToken(requestToken: RequestToken): AccessToken = {
    TwitterOAuth10aAccessToken(
      requestToken.token,
      requestToken.secret
    )
  }

}
