package controllers

import javax.inject.*

import play.api.*
import play.api.mvc.*
import play.api.libs.json.*

@Singleton
class TestController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.obj("message" -> "connected"))
  }

}
