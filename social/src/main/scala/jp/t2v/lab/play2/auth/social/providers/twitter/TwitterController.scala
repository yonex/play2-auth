package jp.t2v.lab.play2.auth.social.providers.twitter

import jp.t2v.lab.play2.auth.social.core.OAuth10aController
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }
import play.api.libs.oauth.RequestToken

abstract class TwitterController[Id, User, Authority] (authConfig: AuthConfig[Id, User, Authority], authenticator: TwitterAuthenticator)
  extends OAuth10aController(authConfig, authenticator)
    with OptionalAuthElement[Id, User, Authority] {

  def requestTokenToAccessToken(requestToken: RequestToken): TwitterOAuth10aAccessToken = {
    TwitterOAuth10aAccessToken(
      requestToken.token,
      requestToken.secret
    )
  }

}
