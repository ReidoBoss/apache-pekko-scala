package actors
package application
package router

import javax.inject.*

import play.api.libs.json.JsValue

import org.apache.pekko.actor.typed.scaladsl.Behaviors

import actors.models.domain.IdWorkspaceWrapper

import models.domain.commands.*
import impl.*
import models.domain.events.*
import models.service.*
import _root_.models.domain.*
import handlers.*

@Singleton
class UserCurrentWorkspaceRouter @Inject (
    userCurrentWorkspaceHandler: UserCurrentWorkspaceHandler
) {
  def handle: PartialFunction[CommandValidated[JsValue], UserResponse] =
    update

  private def update
      : PartialFunction[CommandValidated[JsValue], UserResponse] = {
    case command @ CommandValidated(_, "UpdateUserCurrentWorkspace", _) =>
      command.validate[IdWorkspaceWrapper](userCurrentWorkspaceHandler.update)
      Behaviors.same
  }
}
