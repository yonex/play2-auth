package controllers.ephemeral

import javax.inject.Inject

import controllers.stack.Pjax
import com.github.tototoshi.play2.auth.AuthElement
import play.api.mvc.Controller
import views.html
import com.github.tototoshi.play2.auth.sample.Role._
import play.api.Environment

class Messages @Inject() (val environment: Environment) extends Controller with Pjax with AuthElement with AuthConfigImpl {

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

  protected val fullTemplate: User => Template = html.ephemeral.fullTemplate.apply

}
