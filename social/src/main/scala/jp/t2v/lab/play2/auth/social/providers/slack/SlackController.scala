package jp.t2v.lab.play2.auth.social.providers.slack

import jp.t2v.lab.play2.auth.social.core.{ OAuth2Authenticator, OAuth2Controller }
import jp.t2v.lab.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

abstract class SlackController[Id, User, Authority] (authConfig: AuthConfig[Id, User, Authority], authenticator: OAuth2Authenticator[String])
  extends OAuth2Controller[Id, User, Authority, String](authConfig, authenticator)
    with OptionalAuthElement[Id, User, Authority]