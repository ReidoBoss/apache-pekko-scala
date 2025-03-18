package actors

import scala.concurrent.duration.*

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

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
  ): Behavior[Action] =
    Behaviors.receive { (_, message) =>
      message match {

        case Get(replyTo) =>
          replyTo ! activeUsers
          Behaviors.same

        case Create(actor) =>
          manageUsers(activeUsers + actor)

        case Remove(actor) if activeUsers.size == 1 =>
          Behaviors.stopped

        case Remove(actor) =>
          manageUsers(activeUsers - actor)

        case Terminate =>
          Behaviors.stopped
      }
    }
}
