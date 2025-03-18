package actors
package models
package domain
package commands

import play.api.libs.json.*

import _root_.models.domain.*
import events.*

trait Command[T] {
  val action: String
  val content: T
}
