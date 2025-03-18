package controllers

import javax.inject.*
import java.util.UUID

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.*
import play.api.mvc.*
import play.api.libs.streams.ActorFlow

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.ActorSystem

import actors.impl.*
import actors.application.router.CommandRouter

import models.domain.IdUser

@Singleton
class WebSocketController @Inject() (
)(
    val controllerComponents: ControllerComponents,
    actorSystem: ActorSystem,
    manager: ActorRef[UserManager.Action],
    commandRouter: CommandRouter
)(using ExecutionContext, Scheduler)
    extends BaseController {

  def index: WebSocket = WebSocket.acceptOrResult { request =>
    val idFromHeader = request.id.toString()
    request.session.get("idUser") match
      case None => Future.successful(Left(Forbidden("Forbidden")))
      case Some(value) =>
        wsFutureFlow(idFromHeader, IdUser.fromString(value))
          .map(Right(_))
  }

  private def wsFutureFlow(
      idFromHeader: String,
      idUser: IdUser
  ): Future[Flow[JsValue, JsValue, NotUsed]] = {
    given Timeout = 5.seconds
    val actorName = s"actor-$idFromHeader-$idUser"
    val newUser =
      actorSystem.spawn(UserActor(idUser, manager, commandRouter), actorName)
    newUser.ask(UserActor.Create(idUser, _))
  }
}
