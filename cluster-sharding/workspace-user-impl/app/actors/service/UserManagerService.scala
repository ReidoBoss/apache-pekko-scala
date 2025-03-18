package actors
package service

import javax.inject.*

import org.apache.pekko
import pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import pekko.actor.typed.ActorRef
import pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import pekko.cluster.sharding.typed.scaladsl.Entity
import pekko.cluster.sharding.typed.scaladsl.EntityRef

import impl.*

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
