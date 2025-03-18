package modules

import javax.inject.*

import play.api.libs.concurrent.PekkoGuiceSupport

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter.*

import actors.*
import actors.impl.*

import com.google.inject.AbstractModule

@Singleton
class ActorModule extends AbstractModule with PekkoGuiceSupport {

  override def configure: Unit = {
    bindTypedActor(UserManager, "UserManager")
    bindTypedActor(WorkspaceUserManager, "WorkspaceUserManager")
  }
}
