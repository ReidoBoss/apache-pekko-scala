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

import actors.models.domain.IdWorkspaceWrapper

import models.service.*
import models.domain.commands.*
import impl.*
import models.domain.events.*

@Singleton
class UserWorkspaceHandler @Inject (
    workspaceUserManager: ActorRef[WorkspaceUserManager.Action]
) {

  /**
   * This will let the user leave their current workspace
   *
   * @param command
   * @return
   */
  def delete(command: CommandValidated[IdWorkspaceWrapper]) =
    ???
}
