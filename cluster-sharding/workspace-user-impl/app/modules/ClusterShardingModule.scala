package modules

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class ClusterShardingModule extends AbstractModule {

  /**
   * ClusterSharding Module for Dependency Injection
   */
  @Provides
  @Singleton
  def provideClusterSharding(
      system: ActorSystem[?]
  ): ClusterSharding = ClusterSharding(system)

}
