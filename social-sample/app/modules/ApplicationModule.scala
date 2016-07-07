package modules

import com.google.inject.{ Binder, Module, TypeLiteral }
import controllers.AuthConfigImpl
import jp.t2v.lab.play2.auth.AuthConfig

class ApplicationModule extends Module {


  override def configure(binder: Binder): Unit = {
    binder.bind(new TypeLiteral[AuthConfig[Long, models.User, models.Authority]] () {}).to(classOf[AuthConfigImpl])
  }


}
