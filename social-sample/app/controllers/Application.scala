package controllers

import javax.inject.Inject

import models._
import play.api.mvc.Results._
import play.api.mvc._
import scalikejdbc.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.{ ClassTag, classTag }
import jp.t2v.lab.play2.auth._
import jp.t2v.lab.play2.auth.social.providers.twitter.{ TwitterAuthenticator, TwitterController, TwitterOAuth10aAccessToken, TwitterProviderUserSupport }
import jp.t2v.lab.play2.auth.social.providers.facebook.{ FacebookAuthenticator, FacebookController, FacebookProviderUserSupport }
import jp.t2v.lab.play2.auth.social.providers.github.{ GitHubAuthenticator, GitHubController, GitHubProviderUserSupport }
import jp.t2v.lab.play2.auth.social.providers.slack.{ SlackAuthenticator, SlackController }
import play.api.Environment
import play.api.cache.CacheApi

class Application @Inject() (val authConfig: AuthConfig[Long, models.User, models.Authority])
  extends Controller
    with OptionalAuthElement[Long, models.User, models.Authority] {

  private val logoutHelper = new Logout[Long, models.User, models.Authority](authConfig)

  def index = StackAction { implicit request =>
    DB.readOnly { implicit session =>
      val user = loggedIn
      val gitHubUser = user.flatMap(u => GitHubUser.findByUserId(u.id))
      val facebookUser = user.flatMap(u => FacebookUser.findByUserId(u.id))
      val twitterUser = user.flatMap(u => TwitterUser.findByUserId(u.id))
      val slackAccessToken = user.flatMap(u => SlackAccessToken.findByUserId(u.id))
      Ok(views.html.index(user, gitHubUser, facebookUser, twitterUser, slackAccessToken))
    }
  }

  def logout = Action.async { implicit request =>
    logoutHelper.gotoLogoutSucceeded
  }

}

class AuthConfigImpl @Inject() (environment: Environment, cacheApi: CacheApi) extends AuthConfig[Long, models.User, models.Authority](environment, cacheApi) {

  val idTag: ClassTag[Long] = classTag[Long]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Long)(implicit ctx: ExecutionContext): Future[Option[User]] =
    Future.successful(DB.readOnly { implicit session =>
      User.find(id)
    })

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext) =
    Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    true
  }

}

class FacebookAuthController @Inject() (val authConfig: AuthConfig[Long, models.User, models.Authority])
  extends FacebookController[Long, models.User, models.Authority](authConfig, new FacebookAuthenticator)
    with FacebookProviderUserSupport {

  private val loginHelper = new Login[Long, models.User, models.Authority](authConfig)

  override def onOAuthLinkSucceeded(token: String, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).map { providerUser =>
      DB.localTx { implicit session =>
        FacebookUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def onOAuthLoginSucceeded(token: String)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).flatMap { providerUser =>
      DB.localTx { implicit session =>
        FacebookUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.name, providerUser.coverUrl).id
            FacebookUser.save(id, providerUser)
            loginHelper.gotoLoginSucceeded(id)
          case Some(fu) =>
            loginHelper.gotoLoginSucceeded(fu.userId)
        }
      }
    }
  }

}

class GitHubAuthController @Inject() (val authConfig: AuthConfig[Long, models.User, models.Authority])
  extends GitHubController(authConfig, new GitHubAuthenticator)
    with GitHubProviderUserSupport {

  private val loginHelper = new Login[Long, models.User, models.Authority](authConfig)

  override def onOAuthLinkSucceeded(token: String, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).map { providerUser =>
      DB.localTx { implicit session =>
        GitHubUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def onOAuthLoginSucceeded(token: String)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    retrieveProviderUser(token).flatMap { providerUser =>
      DB.localTx { implicit session =>
        GitHubUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.login, providerUser.avatarUrl).id
            GitHubUser.save(id, providerUser)
            loginHelper.gotoLoginSucceeded(id)
          case Some(gh) =>
            loginHelper.gotoLoginSucceeded(gh.userId)
        }
      }
    }
  }

}

class TwitterAuthController @Inject() (val authConfig: AuthConfig[Long, models.User, models.Authority], authenticator: TwitterAuthenticator)
  extends TwitterController[Long, models.User, models.Authority](authConfig, authenticator)
{

  val userSupport = new TwitterProviderUserSupport(authenticator)

  private val loginHelper = new Login[Long, models.User, models.Authority](authConfig)

  override def onOAuthLinkSucceeded(token: TwitterOAuth10aAccessToken, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    userSupport.retrieveProviderUser(token).map { providerUser =>
      DB.localTx { implicit session =>
        TwitterUser.save(consumerUser.id, providerUser)
        Redirect(routes.Application.index)
      }
    }
  }

  override def onOAuthLoginSucceeded(token: TwitterOAuth10aAccessToken)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    userSupport.retrieveProviderUser(token).flatMap { providerUser =>
      DB.localTx { implicit session =>
        TwitterUser.findById(providerUser.id) match {
          case None =>
            val id = User.create(providerUser.screenName, providerUser.profileImageUrl).id
            TwitterUser.save(id, providerUser)
            loginHelper.gotoLoginSucceeded(id)
          case Some(tu) =>
            loginHelper.gotoLoginSucceeded(tu.userId)
        }
      }
    }
  }

}

class SlackAuthController @Inject() (val authConfig: AuthConfig[Long, models.User, models.Authority])
  extends SlackController(authConfig, new SlackAuthenticator) {

  override def onOAuthLinkSucceeded(accessToken: String, consumerUser: User)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    Future.successful {
      DB.localTx { implicit session =>
        SlackAccessToken.save(consumerUser.id, accessToken)
        Redirect(routes.Application.index)
      }
    }
  }

  override def onOAuthLoginSucceeded(accessToken: String)(implicit request: RequestHeader, ctx: ExecutionContext): Future[Result] = {
    ???
  }

}
