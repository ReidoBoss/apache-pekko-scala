package actors
package impl

import scala.concurrent.duration.*
import scala.util.boundary
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

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
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator.Get as ReplicatorGet
import org.apache.pekko.cluster.ddata.typed.scaladsl.Replicator.Update as ReplicatorUpdate
import org.apache.pekko.util.Timeout
import org.apache.pekko.actor.typed.Scheduler

import cache.WorkspaceUserCache
import com.google.inject.Provides
import _root_.models.domain.*

object WorkspaceUserManager extends ActorModule {
  type Message = Action
  sealed trait Action

  private sealed trait InternalAction extends Action

  final case class Create(
      idWorkspace: IdWorkspace,
      user: (IdUser, ActorRef[UserActor.Action])
  ) extends Action

  final case class Update(
      idWorkspace: IdWorkspace,
      user: (IdUser, ActorRef[UserActor.Action])
  ) extends Action

  final case class Remove(
      idWorkspace: IdWorkspace,
      user: (IdUser, ActorRef[UserActor.Action])
  ) extends Action

  final case class Find(
      idWorkspace: IdWorkspace,
      replyTo: ActorRef[Set[(IdUser, ActorRef[UserActor.Action])]]
  ) extends Action

  final case class FindIdWorkspace(
      user: (IdUser, ActorRef[UserActor.Action]),
      replyTo: ActorRef[Option[IdWorkspace]]
  ) extends Action

  final case class Get(
      idWorkspace: IdWorkspace,
      replyTo: ActorRef[Set[(IdUser, ActorRef[UserActor.Action])]]
  ) extends Action

  private final case class InternalUpdateResponse[A <: ReplicatedData](
      rsp: UpdateResponse[A]
  ) extends InternalAction

  private final case class InternalFindResponse(
      id: IdWorkspace,
      replyTo: ActorRef[Set[(IdUser, ActorRef[UserActor.Action])]],
      response: GetResponse[
        ORMultiMap[IdWorkspace, (IdUser, ActorRef[UserActor.Action])]
      ]
  ) extends InternalAction

  private final case class InternalFindIdWorkspaceResponse(
      user: (IdUser, ActorRef[UserActor.Action]),
      replyTo: ActorRef[Option[IdWorkspace]],
      response: GetResponse[
        ORMultiMap[IdWorkspace, (IdUser, ActorRef[UserActor.Action])]
      ]
  ) extends InternalAction

  @Provides
  def apply(
      cache: WorkspaceUserCache
  ): Behavior[WorkspaceUserManager.Action] =
    Behaviors.setup { context =>
      DistributedData
        .withReplicatorMessageAdapter[
          Action,
          ORMultiMap[IdWorkspace, (IdUser, ActorRef[UserActor.Action])]
        ] { replicator =>

          given node: SelfUniqueAddress =
            DistributedData(context.system).selfUniqueAddress

          val DataKey =
            ORMultiMapKey[IdWorkspace, (IdUser, ActorRef[UserActor.Action])](
              "workspace-users"
            )

          def behavior =
            Behaviors.receiveMessagePartial(
              create
                .orElse(update)
                .orElse(delete)
                .orElse(find)
            )

          def create: PartialFunction[Action, Behavior[Action]] = {
            case Create(id, user) =>
              askUpdate(workspaceMap => internalUpdate(workspaceMap, user, id))

              Behaviors.same
          }

          def update: PartialFunction[Action, Behavior[Action]] = {
            case Update(id, user) =>
              askUpdate(workspaceMap => internalUpdate(workspaceMap, user, id))
              Behaviors.same
          }

          def find: PartialFunction[Action, Behavior[Action]] = {
            case Find(id, replyTo) =>
              replicator.askGet(
                askReplyTo =>
                  ReplicatorGet(
                    DataKey,
                    ReadMajority(DEFAULT_TIMEOUT),
                    askReplyTo
                  ),
                rsp => InternalFindResponse(id, replyTo, rsp)
              )
              Behaviors.same

            case FindIdWorkspace(user, replyTo) =>
              replicator.askGet(
                askReplyTo =>
                  ReplicatorGet(
                    DataKey,
                    ReadMajority(DEFAULT_TIMEOUT),
                    askReplyTo
                  ),
                rsp => InternalFindIdWorkspaceResponse(user, replyTo, rsp)
              )
              Behaviors.same

            case InternalFindIdWorkspaceResponse(
                  user,
                  replyTo,
                  response @ GetSuccess(_, _)
                ) =>
              cache.find(user) match
                case None =>
                  replyTo ! response
                    .get(DataKey)
                    .entries
                    .find(pred => pred._2 == user)
                    .map(_._1)
                case Some(idWorkspace) => replyTo ! Some(idWorkspace)

              Behaviors.same

            case InternalFindResponse(
                  id,
                  replyTo,
                  response @ GetSuccess(_, _)
                ) =>
              response
                .get(DataKey)
                .get(id)
                .map(workspaceUsers => replyTo ! workspaceUsers)
              Behaviors.same
          }

          def delete: PartialFunction[Action, Behavior[Action]] = {
            case Remove(id, user) =>
              askUpdate(workspaceMap => internalDelete(workspaceMap, user, id))
              Behaviors.same
          }

          /**
           * Utility function
           */
          def internalDelete(
              workspaceMap: ORMultiMap[
                IdWorkspace,
                (IdUser, ActorRef[UserActor.Action])
              ],
              user: (IdUser, ActorRef[UserActor.Action]),
              idWorkspace: IdWorkspace
          ) =
            workspaceMap.removeBinding(node, idWorkspace, user)

          /**
           * Utility function
           */
          def internalUpdate(
              workspaceMap: ORMultiMap[
                IdWorkspace,
                (IdUser, ActorRef[UserActor.Action])
              ],
              user: (IdUser, ActorRef[UserActor.Action]),
              idWorkspace: IdWorkspace
          ) =
            cache.find(user) match
              case None =>
                cache.put(user, idWorkspace)
                workspaceMap.addBinding(node, idWorkspace, user)
              case Some(previousIdWorkspace) =>
                cache.put(user, idWorkspace)
                workspaceMap
                  .removeBinding(node, previousIdWorkspace, user)
                  .addBinding(node, idWorkspace, user)

          /**
           * Utility function
           */
          def askUpdate(
              transformer: ORMultiMap[
                IdWorkspace,
                (IdUser, ActorRef[UserActor.Action])
              ] => ORMultiMap[
                IdWorkspace,
                (IdUser, ActorRef[UserActor.Action])
              ]
          ) = replicator.askUpdate(
            askReplyTo =>
              ReplicatorUpdate(
                DataKey,
                ORMultiMap
                  .empty[IdWorkspace, (IdUser, ActorRef[UserActor.Action])],
                WriteMajority(DEFAULT_TIMEOUT),
                askReplyTo
              )(transformer),
            InternalUpdateResponse.apply
          )

          boundary(behavior)

        }
    }

  extension (workspaceManager: ActorRef[WorkspaceUserManager.Action])
    def askFind[T](
        user: (IdUser, ActorRef[UserActor.Action])
    )(transform: Option[IdWorkspace] => T)(using
        Timeout,
        Scheduler,
        ExecutionContext
    ) =
      workspaceManager
        .ask[Option[IdWorkspace]](
          WorkspaceUserManager.FindIdWorkspace(user, _)
        )
        .map(transform)

    def askFind[T](
        idWorkspace: IdWorkspace
    )(transform: Set[(IdUser, ActorRef[UserActor.Action])] => T)(using
        Timeout,
        Scheduler,
        ExecutionContext
    ) =
      workspaceManager
        .ask[Set[(IdUser, ActorRef[UserActor.Action])]](
          WorkspaceUserManager.Find(idWorkspace, _)
        )
        .map(transform)
}
