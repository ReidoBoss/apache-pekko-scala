package actors
package models
package domain
package commands

import play.api.libs.json.JsString

val WorkspaceInvitedUserNotFound = CommandError(
  "WORKSPACE_INVITED_USER_NOT_FOUND"
)

val WorkspaceInviteNotFound = CommandError(
  "WORKSPACE_INVITE_NOT_FOUND"
)

val WorkspaceInviterNotFound = CommandError(
  "WORKSPACE_INVITER_NOT_FOUND",
  JsString("The inviter is not found in the database")
)

val WorkspaceNotFound = CommandError(
  "WORKSPACE_NOT_FOUND",
  JsString("The workspace is not found in the database")
)
