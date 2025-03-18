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
class UserManagerService @Inject (sharding: ClusterSharding) {
  private val UserManagerTypeKey =
    EntityTypeKey[UserManager.Action]("UserManager")

  sharding.init(
    Entity(UserManagerTypeKey)(createBehavior =
      entityContext => UserManager(IdUser.fromString(entityContext.entityId))
    )
  )

  def get(id: IdUser): EntityRef[UserManager.Action] =
    sharding.entityRefFor(UserManagerTypeKey, id.toString())

}
