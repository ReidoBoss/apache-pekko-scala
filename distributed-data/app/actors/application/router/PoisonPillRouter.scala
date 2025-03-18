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
class PoisonPillRouter @Inject (poisonPillHandler: PoisonPillHandler) {

  def handle: PartialFunction[CommandValidated[
    JsValue
  ], UserResponse] = {
    case command @ CommandValidated[JsValue](user, "CreateSelfPoisonPill", _) =>
      poisonPillHandler.create(command)
      Behaviors.same
  }

}
