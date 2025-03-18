package actors
package application
package router

import javax.inject.*

import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Json

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Behavior

import actors.models.domain.commands.*
import actors.impl.*
import actors.models.domain.events.*
import actors.models.service.*

@Singleton
class CommandRouter @Inject (
    workspaceInviteRouter: WorkspaceInviteRouter,
    workspaceMemberRouter: WorkspaceMemberRouter,
    userCurrentWorkspaceRouter: UserCurrentWorkspaceRouter,
    poisonPillRouter: PoisonPillRouter
) {

  def handle: PartialFunction[CommandValidated[JsValue], UserResponse] =
    workspaceInviteRouter.handle
      .orElse(poisonPillRouter.handle)
      .orElse(workspaceMemberRouter.handle)
      .orElse(userCurrentWorkspaceRouter.handle)
      .orElse(unknownRoute)

  private def unknownRoute: PartialFunction[CommandValidated[
    JsValue
  ], UserResponse] = { case unknownCommand =>
    unknownCommand.user._2 ! UserActor.CreateCommandError(
      unknownCommand.toUnkownError
    )
    Behaviors.same
  }

}
