package models
package domain

import java.util.UUID

import play.api.libs.json.*

case class User(
    name: Option[String],
    email: EmailUser,
    username: Username,
    password: String,
    avatar: Option[String] = None,
    id: IdUser = IdUser(UUID.randomUUID)
) {
  def withHashedPassword: User = {
    User(
      name,
      email,
      username,
      utilities.Security.hashPass(password),
      avatar,
      id
    )
  }
  def comparePassword(password: String): Boolean = {
    utilities.Security.comparePass(this.password, password)
  }
}

object User {
  given Writes[User] = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id"       -> user.id,
      "name"     -> user.name,
      "email"    -> user.email,
      "username" -> user.username,
      "avatar"   -> user.avatar
    )
  }
  def apply(
      name: Option[String],
      email: EmailUser,
      username: Username,
      password: String,
      avatar: Option[String]
  ): User = new User(name, email, username, password, avatar)

  def unapply(o: User) = Some(
    (o.name, o.email, o.username, o.password, o.avatar)
  )
}
