package modules

import play.api.libs.concurrent.PekkoGuiceSupport

import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import org.apache.pekko.cluster.sharding.typed.scaladsl.Entity
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityRef

import actors.UserManager
import actors.IdUser
import actors.WorkspaceUserManager
import actors.IdWorkspace

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class ClusterShardingModule extends AbstractModule with PekkoGuiceSupport {
  private val WorkspaceUserManagerTypeKey =
    EntityTypeKey[WorkspaceUserManager.Action]("WorkspaceUserManager")

  private val UserManagerTypeKey =
    EntityTypeKey[UserManager.Action]("UserManager")

  @Provides
  @Singleton
  def provideClusterSharding(
      system: ActorSystem[?]
  ): ClusterSharding = ClusterSharding(system)

  @Provides
  @Singleton
  def initializeUserManager(
      sharding: ClusterSharding
  ) = {

    sharding.init(
      Entity(UserManagerTypeKey)(createBehavior =
        entityContext => UserManager(IdUser.fromString(entityContext.entityId))
      )
    )
  }

  @Provides
  @Singleton
  def initializeWorkspaceUserManager(
      sharding: ClusterSharding
  ) = sharding.init(
    Entity(WorkspaceUserManagerTypeKey)(createBehavior =
      entityContext =>
        WorkspaceUserManager(IdWorkspace.fromString(entityContext.entityId))
    )
  )

  @Provides
  @Singleton
  def provideUserManager(
      sharding: ClusterSharding
  ): IdUser => EntityRef[UserManager.Action] = (idUser: IdUser) =>
    sharding.entityRefFor(UserManagerTypeKey, idUser.toString())

  @Provides
  @Singleton
  def provideWorkspaceUserManager(
      sharding: ClusterSharding
  ): IdWorkspace => EntityRef[WorkspaceUserManager.Action] =
    (idWorkspace: IdWorkspace) =>
      sharding.entityRefFor(WorkspaceUserManagerTypeKey, idWorkspace.toString())

}
