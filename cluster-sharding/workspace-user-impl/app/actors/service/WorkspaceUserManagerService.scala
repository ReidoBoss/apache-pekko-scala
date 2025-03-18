package actors
package service

import javax.inject.*

import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import org.apache.pekko.cluster.sharding.typed.scaladsl.Entity
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityRef

@Singleton
class WorkspaceUserManagerService @Inject (sharding: ClusterSharding) {
  private val WorkspaceUserManagerTypeKey =
    EntityTypeKey[WorkspaceUserManager.Action]("WorkspaceUserManager")

  sharding.init(
    Entity(WorkspaceUserManagerTypeKey)(createBehavior =
      entityContext =>
        WorkspaceUserManager(IdWorkspace.fromString(entityContext.entityId))
    )
  )
  def get(id: IdWorkspace): EntityRef[WorkspaceUserManager.Action] =
    sharding.entityRefFor(WorkspaceUserManagerTypeKey, id.toString())
}
