package actors

import org.apache.pekko.actor.typed.Behavior

package object application {
  import javax.inject.*

  import play.api.libs.json.JsValue
  import play.api.libs.json.Reads

  import org.apache.pekko.actor.typed.ActorRef

  import actors.models.domain.commands.*
  import actors.impl.*
  import actors.models.domain.events.*
  import actors.models.service.*

  type CommanderContext = ActorRef[UserActor.Action]
  type UserResponse =
    Behavior[UserActor.CreateCommandError | UserActor.CreateEvent]

}
