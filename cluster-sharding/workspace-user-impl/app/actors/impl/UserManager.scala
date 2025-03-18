package actors
package impl

import scala.concurrent.duration.*

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Terminated

object UserManager {
  sealed trait Action
  final case class Create(actor: ActorRef[UserActor.Action]) extends Action
  final case class Remove(actor: ActorRef[UserActor.Action]) extends Action
  final case class Get(replyTo: ActorRef[Set[ActorRef[UserActor.Action]]])
      extends Action
  case object Terminate extends Action

  def apply(idUser: IdUser): Behavior[Action] =
    manageUsers(Set.empty)

  private def manageUsers(
      activeUsers: Set[ActorRef[UserActor.Action]]
  ): Behavior[Action] = Behaviors
    .receive[UserManager.Action] { (context, message) =>
      message match {

        case Get(replyTo) =>
          replyTo ! activeUsers
          Behaviors.same

        case Create(actor) =>
          context.watch(actor)
          manageUsers(activeUsers + actor)

        case Remove(actor) if activeUsers.size == 1 =>
          Behaviors.stopped

        case Remove(actor) =>
          manageUsers(activeUsers - actor)

        case Terminate => Behaviors.stopped

      }
    }
    .receiveSignal { case (context, Terminated(actor)) =>
      context.stop(actor)
      activeUsers.size match
        case 1 => Behaviors.stopped
        case _ => manageUsers(activeUsers.filterNot(_ == actor))
    }

}
