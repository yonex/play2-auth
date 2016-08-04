package com.github.tototoshi.play2.auth.social.providers.slack

import com.github.tototoshi.play2.auth.social.core.OAuth2Controller
import com.github.tototoshi.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

trait SlackController extends OAuth2Controller
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new SlackAuthenticator

}