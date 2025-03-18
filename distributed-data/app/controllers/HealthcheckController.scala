package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

@Singleton
class HealthcheckController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    val message = Json.obj(
      "message" -> "connected"
    )
    Ok(message)
  }
}
