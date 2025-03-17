package modules

import play.api.libs.concurrent.PekkoGuiceSupport

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter.*

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class ActorModules extends AbstractModule with PekkoGuiceSupport {

  @Provides
  @Singleton
  def provideTypedActorSystem(
      system: ActorSystem
  ): org.apache.pekko.actor.typed.ActorSystem[?] = system.toTyped

}
