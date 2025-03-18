package actors
package models
package domain
package events

import play.api.libs.json.JsValue
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.JsNull

case class Event(
    action: String,
    content: JsValue = JsNull
) {
  def toJson = Json.toJson(this)
}

object Event {
  given Writes[Event] = Json.writes[Event]

  def apply(action: String, jsonMap: (String, String)*) =
    new Event(action, Json.toJson(jsonMap.toMap))

  def invalid(json: JsValue): Event = Event(
    "InvalidActionEvent",
    json
  )

  def invalid(jsonMap: (String, String)*): Event = Event(
    "InvalidActionEvent",
    Json.toJson(jsonMap.toMap)
  )
}

opaque type EventSuccess = Event
object EventSuccess {

  def apply(action: String, jsonMap: (String, String)*): EventSuccess =
    new Event(action, Json.toJson(jsonMap.toMap))

  extension (x: EventSuccess) def value: Event = x
}

opaque type EventError = Event
object EventError {

  def apply(action: String): EventError =
    new Event(action)

  def apply(action: String, jsonMap: (String, String)*): EventError =
    new Event(action, Json.toJson(jsonMap.toMap))

  def invalid(json: JsValue): EventSuccess = Event(
    "InvalidActionEvent",
    json
  )

  def invalid(jsonMap: (String, String)*): Event = Event(
    "InvalidActionEvent",
    Json.toJson(jsonMap.toMap)
  )

  extension (x: EventError) def value: Event = x
}
