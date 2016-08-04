package com.github.tototoshi.play2.auth.social.providers.github

case class GitHubUser(
  id: Long,
  login: String,
  avatarUrl: String,
  accessToken: String)
