package com.github.tototoshi.play2.auth.social.providers.twitter

case class TwitterUser(
  id: Long,
  screenName: String,
  name: String,
  description: String,
  profileImageUrl: String,
  accessToken: String,
  accessTokenSecret: String)
