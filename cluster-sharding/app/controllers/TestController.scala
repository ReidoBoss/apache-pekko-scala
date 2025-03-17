package controllers

import javax.inject.*

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext

import play.api.*
import play.api.mvc.*
import play.api.libs.json.*

import org.apache.pekko

import actors.*

import pekko.actor.typed.Scheduler
import pekko.actor.ActorSystem
import pekko.actor.typed.scaladsl.adapter.*
import pekko.actor.typed.scaladsl.AskPattern.Askable
import pekko.util.Timeout

@Singleton
class TestController @Inject() (
    val controllerComponents: ControllerComponents,
    actorSystem: ActorSystem
)(using Scheduler, ExecutionContext)
    extends BaseController {

  given Timeout = 1.seconds

  val actorOne = actorSystem.spawn(Counter("1"), "counter1")

  def index() = Action { implicit request: Request[AnyContent] =>
    actorOne ! Counter.Increment
    actorOne.ask(Counter.GetValue(_)).map(println)

    Ok(Json.obj("message" -> "connected"))
  }

}
