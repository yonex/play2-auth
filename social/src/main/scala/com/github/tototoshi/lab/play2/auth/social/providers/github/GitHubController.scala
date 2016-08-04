package com.github.tototoshi.play2.auth.social.providers.github

import com.github.tototoshi.play2.auth.social.core.OAuth2Controller
import com.github.tototoshi.play2.auth.{ AuthConfig, Login, OptionalAuthElement }

trait GitHubController extends OAuth2Controller
    with AuthConfig
    with OptionalAuthElement
    with Login {

  val authenticator = new GitHubAuthenticator

}