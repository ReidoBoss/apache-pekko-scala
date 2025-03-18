package actors
package impl

import javax.inject.*
import java.util.UUID

import scala.collection.Seq
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.*

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.stream.scaladsl.Flow
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.stream.scaladsl.MergeHub
import org.apache.pekko.stream.scaladsl.BroadcastHub
import org.apache.pekko.stream.scaladsl.Keep
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.Done

import actors.models.domain.commands.CommandValidated
import actors.models.domain.commands.PartialCommandJson
import actors.models.domain.events.Event

import models.domain.commands.*
import _root_.models.domain.*
import com.google.inject.Provides
import application.router.CommandRouter

class UserActor @Inject (
    id: IdUser,
    manager: ActorRef[UserManager.Action],
    commandRouter: CommandRouter
)(using context: ActorContext[UserActor.Action]) {
  import UserActor.Action

  val system                 = context.system
  given Timeout              = 3.seconds
  given ActorSystem[Nothing] = system

  val (hubSink, hubSource) = MergeHub
    .source[JsValue](16)
    .toMat(BroadcastHub.sink(256))(Keep.both)
    .run()

  /**
   * This is where we see the client's or the frontend messages
   */

  private val jsonSink: Sink[JsValue, Future[Done]] = Sink.foreach { json =>
    json
      .validate[PartialCommandJson]
      .fold(
        error => sendToClient(actors.util.validationErrorsJson(error)),
        partialCommand => commandRouter.handle(partialCommand(id, context.self))
      )
  }

  def behaviors: Behavior[UserActor.Action] =
    Behaviors.receiveMessage {

      case UserActor.Create(id, replyTo) =>
        manager ! UserManager.Create(id, context.self)
        replyTo ! websocketFlow
        Behaviors.same

      case UserActor.CreateEvent(event) =>
        sendToClient(event.toJson)
        Behaviors.same

      case UserActor.CreateCommandError(commandError) =>
        sendToClient(commandError.toJson)
        Behaviors.same

      case UserActor.Terminate =>
        manager ! UserManager.Remove(id, context.self)
        PoisonPill
        Behaviors.stopped
    }

  private def PoisonPill =
    val poisonCommand = CommandValidated[JsValue](
      (id, context.self),
      "CreateSelfPoisonPill",
      Json.obj()
    )

    commandRouter.handle(poisonCommand)

  private def sendToClient(json: JsValue) =
    Source.single(json).runWith(hubSink)

  private val websocketFlow: Flow[JsValue, JsValue, NotUsed] = Flow
    .fromSinkAndSourceCoupled(jsonSink, hubSource)
    .watchTermination() { (_, termination) =>
      context.pipeToSelf(termination)(_ => UserActor.Terminate)
      NotUsed
    }

}

object UserActor {
  sealed trait Action

  final case class Create(
      id: IdUser,
      replyTo: ActorRef[Flow[JsValue, JsValue, NotUsed]]
  ) extends Action

  final case class CreateEvent(event: Event) extends Action

  final case class CreateCommandError(command: CommandError) extends Action

  case object Terminate extends Action

  @Provides
  def apply(
      id: IdUser,
      manager: ActorRef[UserManager.Action],
      commandRouter: CommandRouter
  ): Behavior[UserActor.Action] = Behaviors.setup { implicit context =>
    new UserActor(id, manager, commandRouter).behaviors
  }
}
