package actors
package models
package domain
package commands

import play.api.libs.json.*

import _root_.models.domain.*

case class CommandError(
    action: String,
    content: JsValue = JsNull
) extends Command[JsValue] {

  def toJson = Json.obj(
    "action"  -> action,
    "content" -> content
  )

}
