package jp.t2v.lab.play2.auth.social.providers.facebook

import jp.t2v.lab.play2.auth.social.core.{ OAuth2Authenticator, OAuth2Controller }
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

abstract class FacebookController[Id, User, Authority, AccessToken] (authConfig: AuthConfig[Id, User, Authority], authenticator: OAuth2Authenticator[AccessToken])
  extends OAuth2Controller[Id, User, Authority, AccessToken](authConfig, authenticator)
    with OptionalAuthElement[Id, User, Authority]

