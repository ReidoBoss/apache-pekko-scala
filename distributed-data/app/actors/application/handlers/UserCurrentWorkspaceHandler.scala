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
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable

import actors.models.domain.IdWorkspaceWrapper

import models.service.*
import models.domain.commands.*
import impl.*
import models.domain.events.*

@Singleton
class UserCurrentWorkspaceHandler @Inject() (
    workspaceUserManager: ActorRef[WorkspaceUserManager.Action]
) {

  def update(command: CommandValidated[IdWorkspaceWrapper]) =
    workspaceUserManager ! WorkspaceUserManager.Update(
      command.content.idWorkspace,
      command.user
    )
}
