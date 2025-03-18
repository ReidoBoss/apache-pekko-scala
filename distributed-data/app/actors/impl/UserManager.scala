package actors
package impl

import scala.concurrent.duration.*
import scala.util.boundary
import scala.concurrent.ExecutionContext

import play.api.libs.concurrent.ActorModule

import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.cluster.ddata.*
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.ddata.Replicator.*
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator
import org.apache.pekko.cluster.ddata.SelfUniqueAddress
import org.apache.pekko.cluster.ddata.typed.scaladsl.DistributedData
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator.Get
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator.Update
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.Scheduler

import com.google.inject.Provides
import _root_.models.domain.*
object UserManager extends ActorModule {

  type Message = Action

  sealed trait Action
  private sealed trait InternalAction extends Action

  final case class Create(
      id: IdUser,
      actor: ActorRef[UserActor.Action]
  ) extends Action

  final case class Remove(
      id: IdUser,
      actor: ActorRef[UserActor.Action]
  ) extends Action

  final case class Find(
      id: IdUser,
      replyTo: ActorRef[Set[ActorRef[UserActor.Action]]]
  ) extends Action

  private final case class InternalFindResponse(
      id: IdUser,
      replyTo: ActorRef[Set[ActorRef[UserActor.Action]]],
      response: GetResponse[ORMultiMap[IdUser, ActorRef[UserActor.Action]]]
  ) extends InternalAction
  private final case class InternalUpdateResponse[A <: ReplicatedData](
      rsp: UpdateResponse[A]
  ) extends InternalAction

  @Provides
  def apply(): Behavior[UserManager.Action] = Behaviors.setup { context =>
    DistributedData
      .withReplicatorMessageAdapter[
        Action,
        ORMultiMap[IdUser, ActorRef[UserActor.Action]]
      ] { replicator =>
        given node: SelfUniqueAddress =
          DistributedData(context.system).selfUniqueAddress

        val DataKey =
          ORMultiMapKey[IdUser, ActorRef[UserActor.Action]]("user-actors")

        def behavior = Behaviors.receiveMessagePartial(
          create
            .orElse(delete)
            .orElse(find)
        )

        def find: PartialFunction[Action, Behavior[Action]] = {
          case Find(id, replyTo) =>
            replicator.askGet(
              askReplyTo =>
                Get(DataKey, ReadMajority(DEFAULT_TIMEOUT), askReplyTo),
              rsp => InternalFindResponse(id, replyTo, rsp)
            )
            Behaviors.same

          case InternalFindResponse(
                idUser,
                replyTo,
                response @ GetSuccess(_, _)
              ) =>
            response
              .get(DataKey)
              .get(idUser)
              .map(actorsOfUser => replyTo ! actorsOfUser)
            Behaviors.same
        }

        def delete: PartialFunction[Action, Behavior[Action]] = {
          case Remove(id, actor) =>
            askUpdate(users => remove(users, actor, id))
            Behaviors.same
        }

        def create: PartialFunction[Action, Behavior[Action]] = {
          case Create(id, actor) =>
            askUpdate(users => update(users, actor, id))
            Behaviors.same
        }

        def remove(
            userMap: ORMultiMap[IdUser, ActorRef[UserActor.Action]],
            actorRef: ActorRef[UserActor.Action],
            idUser: IdUser
        ): ORMultiMap[IdUser, ActorRef[UserActor.Action]] =
          userMap.removeBinding(node, idUser, actorRef)

        def update(
            userMap: ORMultiMap[IdUser, ActorRef[UserActor.Action]],
            actorRef: ActorRef[UserActor.Action],
            idUser: IdUser
        ): ORMultiMap[IdUser, ActorRef[UserActor.Action]] =
          userMap.addBinding(node, idUser, actorRef)

        def askUpdate(
            transformer: ORMultiMap[
              IdUser,
              ActorRef[UserActor.Action]
            ] => ORMultiMap[
              IdUser,
              ActorRef[UserActor.Action]
            ]
        ) = replicator.askUpdate(
          askReplyTo =>
            Update(
              DataKey,
              ORMultiMap.empty[IdUser, ActorRef[UserActor.Action]],
              WriteMajority(DEFAULT_TIMEOUT),
              askReplyTo
            )(transformer),
          InternalUpdateResponse.apply
        )
        boundary(behavior)
      }

  }

  extension (manager: ActorRef[UserManager.Action])
    def askFind[T](
        id: IdUser
    )(transform: Set[ActorRef[UserActor.Action]] => T)(using
        Timeout,
        Scheduler,
        ExecutionContext
    ) =
      manager
        .ask[Set[ActorRef[UserActor.Action]]](UserManager.Find(id, _))
        .map(transform)

}
