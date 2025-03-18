package actors
package cache
import javax.inject.*
import java.util.concurrent.TimeUnit

import org.apache.pekko.actor.typed.ActorRef

import actors.impl.UserActor

import _root_.models.domain.IdUser
import _root_.models.domain.IdWorkspace
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine

@Singleton
class WorkspaceUserCache {

  private val cache: Cache[(IdUser, ActorRef[UserActor.Action]), IdWorkspace] =
    Caffeine
      .newBuilder()
      .build()

  def put(
      key: (IdUser, ActorRef[UserActor.Action]),
      value: IdWorkspace
  ): WorkspaceUserCache =
    cache.put(key, value)
    this

  def find(key: (IdUser, ActorRef[UserActor.Action])): Option[IdWorkspace] =
    Option(cache.getIfPresent(key))

  def delete(
      key: (IdUser, ActorRef[UserActor.Action])
  ): WorkspaceUserCache =
    cache.invalidate(key)
    this

}
