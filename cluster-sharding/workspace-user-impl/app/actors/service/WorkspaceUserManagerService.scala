package actors
package service

import javax.inject.*

import org.apache.pekko
import pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import pekko.cluster.sharding.typed.scaladsl.Entity
import pekko.cluster.sharding.typed.scaladsl.EntityRef

import impl.*

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
