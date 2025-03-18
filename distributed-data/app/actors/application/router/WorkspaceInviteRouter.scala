package actors
package application
package router

import javax.inject.*

import scala.concurrent.ExecutionContext

import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Json

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.util.Timeout

import models.domain.commands.*
import impl.*
import models.domain.events.*
import models.service.*
import _root_.models.domain.*
import handlers.*

@Singleton
class WorkspaceInviteRouter @Inject (
    workspaceInviteHandler: WorkspaceInviteHandler,
    workspaceConnectionHandler: WorkspaceMemberHandler
)(using Scheduler, ExecutionContext) {

  def handle: PartialFunction[CommandValidated[JsValue], UserResponse] =
    create

  private def create
      : PartialFunction[CommandValidated[JsValue], UserResponse] = {
    case command @ CommandValidated(_, "CreateWorkspaceInvite", _) =>
      command.validate[WorkspaceMemberCreate](workspaceInviteHandler.create)
      Behaviors.same
  }

}
