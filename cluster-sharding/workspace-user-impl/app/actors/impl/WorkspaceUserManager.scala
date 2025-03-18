package actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object WorkspaceUserManager {
  sealed trait Action
  final case class Create(user: (IdUser, ActorRef[WorkspaceUserManager.Action]))
      extends Action
  final case class Remove(user: ActorRef[WorkspaceUserManager.Action])
      extends Action
  final case class Get(
      replyTo: ActorRef[Set[(IdUser, ActorRef[WorkspaceUserManager.Action])]]
  ) extends Action
  case object Terminate extends Action

  def apply(
      idWorkspace: IdWorkspace
  ): Behavior[Action] = manageWorkspaceUsers(Set.empty)

  private def manageWorkspaceUsers(
      users: Set[(IdUser, ActorRef[WorkspaceUserManager.Action])]
  ): Behavior[Action] = {
    Behaviors.receive { (context, message) =>
      message match
        case Get(replyTo) =>
          replyTo ! users
          Behaviors.same

        case Create(user) =>
          manageWorkspaceUsers(users + user)

        case Remove(user) if users.size == 1 =>
          Behaviors.stopped

        case Remove(actor) =>
          manageWorkspaceUsers(users.filter(_._2 != actor))

        case Terminate =>
          Behaviors.stopped

    }
  }

}
