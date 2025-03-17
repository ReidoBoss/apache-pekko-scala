package controllers

import javax.inject.*

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext

import play.api.*
import play.api.mvc.*
import play.api.libs.json.*

import org.apache.pekko
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.cluster.sharding.typed.scaladsl.Entity

import actors.*

import pekko.actor.typed.Scheduler
import pekko.actor.typed.scaladsl.adapter.*
import pekko.actor.typed.scaladsl.AskPattern.Askable
import pekko.util.Timeout
import pekko.cluster.sharding.typed.ShardingEnvelope
import pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import pekko.cluster.sharding.typed.scaladsl.EntityRef

@Singleton
class TestController @Inject() (
    val controllerComponents: ControllerComponents,
    system: ActorSystem[?],
)(using Scheduler, ExecutionContext)
    extends BaseController {

  private val sharding  = ClusterSharding(system)
  private val sharding2 = ClusterSharding(system)

  given Timeout = 1.seconds

  val TypeKey = EntityTypeKey[Counter.Command]("Counter")

  sharding.init(
    Entity(TypeKey)(createBehavior = entityContext =>
      println(entityContext.entityId)
      Counter(entityContext.entityId)
    )
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    sharding.entityRefFor(TypeKey, "counter-1") ! Counter.Increment
    Ok(Json.obj("message" -> "connected"))
  }

}
