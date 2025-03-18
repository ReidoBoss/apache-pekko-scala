package actors
package models
package service

import javax.inject.*
import java.util.UUID

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.Json

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.Scheduler
import cats.data.*
import cats.implicits.*

import actors.impl.UserActor.CreateEvent

import models.domain.commands.*
import impl.*
import models.domain.events.*
import models.service.*
import _root_.models.repo.*
import _root_.models.service.*
import _root_.models.domain.*

@Singleton
class WSWorkspaceInviteService @Inject (
    userManager: ActorRef[UserManager.Action],
    userRepo: UserRepo,
    memberRepo: WorkspaceMemberRepo,
    workspaceRepo: WorkspaceRepo
)(using Scheduler, ExecutionContext) {

  def create(
      command: CommandValidated[WorkspaceMemberCreate]
  ): EitherT[Future, CommandError, WorkspaceInviteDetails] =
    for {
      invitedUser <- OptionT(
        userRepo.Table.find(command.content.credentialInvited)
      ).toRight(WorkspaceInvitedUserNotFound)

      inviter <- OptionT(userRepo.Table.find(command.user._1))
        .toRight(WorkspaceInviterNotFound)

      member <- OptionT(
        memberRepo.Table.find(
          invitedUser.id,
          command.content.idWorkspace
        )
      ).toRight(WorkspaceInviteNotFound)

      workspace <- OptionT(
        workspaceRepo.Table.find(command.content.idWorkspace)
      ).toRight(WorkspaceNotFound)

      result <- EitherT.pure(
        member.toWorkspaceInviteDetails(inviter.username, workspace.name)
      )

    } yield result

}
