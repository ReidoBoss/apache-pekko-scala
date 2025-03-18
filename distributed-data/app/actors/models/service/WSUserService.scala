package actors
package models
package service

import javax.inject.*

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import cats.data.*
import cats.implicits.*

import domain.commands.*

import _root_.models.repo.*
import _root_.models.domain.*

@Singleton
class WSUserService @Inject (userRepo: UserRepo)(using
    ExecutionContext
) {
  def find(id: IdUser): EitherT[Future, CommandError, User] = for {
    user   <- OptionT(userRepo.Table.find(id)).toRight(UserNotFound)
    result <- EitherT.pure(user)
  } yield result
}
