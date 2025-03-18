package actors
package application
package handlers

import javax.inject.*

import scala.concurrent.ExecutionContext

import play.api.libs.json.*

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Scheduler

import actors.impl.WorkspaceUserManager
import actors.impl.UserManager

import models.domain.commands.CommandValidated

@Singleton
class PoisonPillHandler @Inject (
    workspaceUserManager: ActorRef[WorkspaceUserManager.Action],
    userManager: ActorRef[UserManager.Action]
)(using Scheduler, ExecutionContext) {

  def create(command: CommandValidated[JsValue]) = {
    userManager ! UserManager.Remove(command.user._1, command.user._2)
    workspaceUserManager
      .askFind(command.user)(
        _.map(idWorkspace =>
          workspaceUserManager ! WorkspaceUserManager.Remove(
            idWorkspace,
            command.user
          )
        )
      )
  }
}
