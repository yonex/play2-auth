package controllers.ephemeral

import controllers.stack.Pjax
import jp.t2v.lab.play2.auth.{ AuthConfig, AuthElement }
import jp.t2v.lab.play2.auth.sample.{ Account, Role }
import play.api.mvc.Controller
import views.html
import jp.t2v.lab.play2.auth.sample.Role._

trait Messages extends Controller with Pjax with AuthElement[Int, Account, Role] {

  val authConfig: AuthConfig[Int, Account, Role] = new AuthConfigImpl {}

  def main = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val title = "message main"
    Ok(html.message.main(title))
  }

  def list = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val title = "all messages"
    Ok(html.message.list(title))
  }

  def detail(id: Int) = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val title = "messages detail "
    Ok(html.message.detail(title + id))
  }

  def write = StackAction(AuthorityKey -> Administrator) { implicit request =>
    val title = "write message"
    Ok(html.message.write(title))
  }

  protected val fullTemplate: Account => Template = html.ephemeral.fullTemplate.apply

}
object Messages extends Messages
