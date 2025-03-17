package actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object WorkspaceUserManager {
  sealed trait Action
  final case class Create(actor: (IdUser, ActorRef[UserActor.Action]))
      extends Action
  final case class Remove(actor: (IdUser, ActorRef[UserActor.Action]))
      extends Action
  case object Terminate extends Action

  def apply(idWorkspace: IdWorkspace): Behavior[Action] = {
    def states(
        actors: Seq[(IdUser, ActorRef[UserActor.Action])]
    ): Behavior[Action] = {
      Behaviors.setup { context =>
        Behaviors.receiveMessage[Action] {

          case Create(actor) =>
            states(actors :+ actor)

          case Remove(actor) if actors.length == 1 =>
            Behaviors.stopped

          case Remove(actor) =>
            states(actors.filter(_ != actor))

          case Terminate =>
            Behaviors.stopped

        }
      }
    }
    states(Seq.empty)
  }
}
