package actors
package models
package domain
package commands

import play.api.libs.json.*

import org.apache.pekko.actor.typed.ActorRef

import actors.models.domain.events.Event
import actors.impl.UserActor

import _root_.models.domain.IdUser

case class CommandValidated[T](
    user: (IdUser, ActorRef[UserActor.Action]),
    action: String,
    content: T
) extends Command[T] {

  def toError: CommandError = new CommandError(action)
  def toUnkownError: CommandError =
    new CommandError("UnkownCommand", JsString(action))

  /**
   * @param doneAction
   *   make sure this will return a word indicating that the command is accepted
   *   * **Example:** `CreateInviteUser` is a command action while
   *   `CreatedInviteUser` is an Event because it is now accepted.
   * @return
   *   an Event
   */
  def toEvent(doneAction: String)(using Writes[T]) =
    Event(doneAction, Json.toJson(content))

  /**
   * Even though this function does not have anything to do with the contents in
   * Command[T], this will be more semantic to use
   * @param doneAction
   *   make sure this will return a word indicating that the command is accepted
   *   * **Example:** `InviteUser` is a command action while `InvitedUser` is an
   *   Event because it is now accepted.
   * @return
   *   an Event
   */
  def toEvent[R](doneAction: String, newContent: R)(using Writes[R]) =
    Event(doneAction, Json.toJson(newContent))
}

type PartialCommandJson =
  ((IdUser, ActorRef[UserActor.Action])) => CommandValidated[JsValue]

object CommandValidated {
  given Reads[PartialCommandJson] =
    new Reads[PartialCommandJson] {
      def reads(json: JsValue): JsResult[PartialCommandJson] = for {
        action  <- (json \ "action").validate[String]
        content <- (json \ "content").validate[JsValue]
      } yield CommandValidated(_, action, content)
    }

  extension (commandJson: CommandValidated[JsValue])
    def validate[T](transformer: CommandValidated[T] => Any)(using
        reads: Reads[T]
    ) = commandJson.content
      .validate[T]
      .fold(
        error =>
          commandJson.user._2 ! UserActor.CreateEvent(
            Event.invalid(actors.util.validationErrorsJson(error))
          ),
        validatedContent =>
          transformer(commandJson.copy[T](content = validatedContent))
      )

}
