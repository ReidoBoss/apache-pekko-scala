package actors
package models
package domain
package commands

import play.api.libs.json.JsString

val UserNotFound = CommandError(
  "USER_NOT_FOUND",
  JsString("You are the user but your id is not found in the database")
)
