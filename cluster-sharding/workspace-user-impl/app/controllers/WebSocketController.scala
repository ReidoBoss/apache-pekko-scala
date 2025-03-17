package controllers

import javax.inject.*
import java.util.UUID

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.*
import play.api.mvc.*
import play.api.mvc.Results.*
import play.api.libs.streams.ActorFlow

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.cluster.sharding.ClusterSharding

import actors.*

@Singleton
class WebSocketController @Inject() (
    actorSystem: ActorSystem
)(using ExecutionContext, Scheduler) {

  given Timeout = 30.seconds

  def index: WebSocket = WebSocket.acceptOrResult { request =>
    val idFromHeader = request.id.toString()
    request.session.get("idUser") match
      case None => Future.successful(Left(Forbidden("Forbidden")))
      case Some(idUser) =>
        wsFutureFlow(IdUser.fromString(idUser), idFromHeader)
          .map(Right(_))
  }

  private def wsFutureFlow(
      idUser: IdUser,
      idFromHeader: String,
  ): Future[Flow[JsValue, JsValue, NotUsed]] = {
    val actorName = s"actor-$idFromHeader-$idUser"
    val userActor = actorSystem.spawn(UserActor.apply(idUser), actorName)
    userActor.ask(UserActor.Create(_))
  }
}
