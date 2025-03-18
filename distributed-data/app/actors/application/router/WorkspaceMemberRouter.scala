package actors
package application
package router

import javax.inject.*

import play.api.libs.json.*

import org.apache.pekko.actor.typed.scaladsl.Behaviors

import actors.models.domain.IdWorkspaceWrapper

import models.domain.commands.*
import handlers.*
import _root_.models.domain.IdWorkspace

@Singleton
class WorkspaceMemberRouter @Inject (
    workspaceMemberHandler: WorkspaceMemberHandler
) {
  def handle: PartialFunction[CommandValidated[JsValue], UserResponse] =
    create

  private def create
      : PartialFunction[CommandValidated[JsValue], UserResponse] = {
    case command @ CommandValidated(_, "CreateWorkspaceMember", _) =>
      command.validate[java.util.UUID](workspaceMemberHandler.update)
      Behaviors.same
  }

}
