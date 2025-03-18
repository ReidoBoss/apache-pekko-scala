package actors
package application
package handlers

import javax.inject.*

import scala.concurrent.ExecutionContext

import play.api.libs.json.Json

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.util.Timeout
import cats.data.*
import cats.implicits.*

import models.service.*
import models.domain.commands.*
import impl.*
import models.domain.events.*
import _root_.models.domain.WorkspaceMemberCreate

@Singleton
class WorkspaceInviteHandler @Inject (
    wSWorkspaceInviteService: WSWorkspaceInviteService,
    userManager: ActorRef[UserManager.Action]
)(using ExecutionContext, Scheduler) {

  def create(inviteCommand: CommandValidated[WorkspaceMemberCreate]) =
    wSWorkspaceInviteService
      .create(inviteCommand)
      .fold(
        error => inviteCommand.user._2 ! UserActor.CreateCommandError(error),
        workspaceInviteDetails =>
          userManager.askFind(workspaceInviteDetails.idUser) { userActors =>
            userActors.map(userActor =>
              userActor ! UserActor.CreateEvent(
                new Event(
                  "CreatedWorkspaceInvite",
                  Json.toJson(workspaceInviteDetails)
                )
              )
            )
          }
      )
}
