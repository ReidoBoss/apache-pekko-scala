package actors
package application
package handlers

import javax.inject.*

import scala.concurrent.ExecutionContext

import play.api.libs.json.*

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Scheduler

import impl.WorkspaceUserManager
import impl.UserActor
import models.domain.events.*
import models.domain.commands.*
import _root_.models.domain.*
import models.service.*

@Singleton
class WorkspaceMemberHandler @Inject (
    workspaceUserManager: ActorRef[WorkspaceUserManager.Action],
    wSUserService: WSUserService
)(using Scheduler, ExecutionContext) {

  def update(command: CommandValidated[java.util.UUID]) =
    wSUserService
      .find(command.user._1)
      .fold(
        error => command.user._2 ! UserActor.CreateCommandError(error),
        user =>
          workspaceUserManager.askFind(IdWorkspace(command.content)) { users =>
            users.map { (_, actor) =>
              actor ! UserActor.CreateEvent(
                Event("CreatedWorkspaceMember", Json.toJson(user))
              )
            }
          }
      )

}
