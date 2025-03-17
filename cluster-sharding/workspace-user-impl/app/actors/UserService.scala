package actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors

object UserService {
  sealed trait Action
  case object Increment extends Action

  def apply(idUser: String): Behavior[Action] = {
    def updated(value: Int): Behavior[Action] = {
      Behaviors.receiveMessage[Action] { case Increment =>
        updated(value + 1)
      }
    }
    updated(0)

  }
}
