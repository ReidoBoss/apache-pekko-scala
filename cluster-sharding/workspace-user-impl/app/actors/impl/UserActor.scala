package actors
package impl

import javax.inject.*

import scala.concurrent.duration.*
import scala.concurrent.Future

import play.api.libs.json.*

import org.apache.pekko
import pekko.actor.typed.ActorRef
import pekko.actor.typed.scaladsl.ActorContext
import pekko.util.Timeout
import pekko.stream.scaladsl.MergeHub
import pekko.stream.scaladsl.BroadcastHub
import pekko.stream.scaladsl.Keep
import pekko.stream.scaladsl.Flow
import pekko.NotUsed
import pekko.Done
import pekko.stream.scaladsl.Sink
import pekko.actor.typed.Behavior
import pekko.actor.typed.scaladsl.Behaviors
import pekko.stream.scaladsl.Source
import pekko.actor.typed.ActorSystem

private case class UserActor @Inject() (
    id: IdUser,
    currentIdWorkspace: Option[IdWorkspace] = None,
)(using val context: ActorContext[UserActor.Action])
    extends UserActorMixin {
  import UserActor.*
  given system: ActorSystem[Nothing] = context.system

  /**
   * this is the json sent from the client
   */
  val jsonSink: Sink[JsValue, Future[Done]] =
    Sink.foreach(json => println(json))

  def behaviors: Behavior[UserActor.Action] =
    Behaviors.receiveMessage {

      case Create(replyTo) =>
        replyTo ! websocketFlow
        Behaviors.same

      case event @ CreateEvent(_, _) =>
        sendToClient(event.toJson)
        Behaviors.same

      case Terminate =>
        currentIdWorkspace.map(x => ???)
        Behaviors.stopped
    }

}

object UserActor {
  def apply(id: IdUser): Behavior[UserActor.Action] =
    Behaviors.setup { implicit context =>
      new UserActor(id).behaviors
    }

  sealed trait Action
  /* Commands */
  sealed trait Command  extends Action
  case object Terminate extends Command
  final case class Create(
      replyTo: ActorRef[Flow[JsValue, JsValue, NotUsed]]
  ) extends Command
  /* Events */
  sealed trait Event extends Action
  final case class CreateEvent(
      action: String,
      content: JsValue
  ) extends Event {
    def toJson = Json.obj(
      "action"  -> action,
      "content" -> content
    )
  }

}

private trait UserActorMixin {

  implicit def system: ActorSystem[Nothing]

  val context: ActorContext[UserActor.Action]

  val (hubSink, hubSource) = MergeHub
    .source[JsValue](16)
    .toMat(BroadcastHub.sink(256))(Keep.both)
    .run()

  val jsonSink: Sink[JsValue, Future[Done]]

  val websocketFlow: Flow[JsValue, JsValue, NotUsed] = Flow
    .fromSinkAndSourceCoupled(jsonSink, hubSource)
    .watchTermination() { (_, termination) =>
      context.pipeToSelf(termination)(_ => UserActor.Terminate)
      NotUsed
    }

  def sendToClient(json: JsValue) =
    Source.single(json).runWith(hubSink)
}
