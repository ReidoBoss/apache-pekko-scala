package modules

import play.api.libs.concurrent.PekkoGuiceSupport

import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import org.apache.pekko.cluster.sharding.typed.scaladsl.Entity

import actors.UserManager
import actors.IdUser

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class ClusterShardingModule extends AbstractModule with PekkoGuiceSupport {

  @Provides
  @Singleton
  def provideClusterSharding(
      system: ActorSystem[?]
  ): ClusterSharding = ClusterSharding(system)

  @Provides
  @Singleton
  def provideUserManager(
      sharding: ClusterSharding
  ): ActorRef[ShardingEnvelope[UserManager.Action]] =
    val TypeKey = EntityTypeKey[UserManager.Action]("user-actor-manager")
    sharding.init(
      Entity(TypeKey)(entityContext =>
        UserManager(IdUser.fromString(entityContext.entityId))
      )
    )
}
