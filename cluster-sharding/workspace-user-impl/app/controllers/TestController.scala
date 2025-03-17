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
import pekko.actor.typed.Scheduler
import pekko.actor.typed.scaladsl.adapter.*
import pekko.actor.typed.scaladsl.AskPattern.Askable
import pekko.util.Timeout
import pekko.cluster.sharding.typed.ShardingEnvelope
import pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import pekko.cluster.sharding.typed.scaladsl.EntityRef

import actors.*

@Singleton
class TestController @Inject() (
    val controllerComponents: ControllerComponents,
    sharding: ClusterSharding
)(using Scheduler, ExecutionContext)
    extends BaseController {

  given Timeout = 1.seconds

  val TypeKey = EntityTypeKey[Counter.Command]("Counter")
  // val TypeKey2 = EntityTypeKey[Counter.Command]("Counter")
  val test = sharding.init(
    Entity(TypeKey)(createBehavior =
      entityContext => Counter(entityContext.entityId)
    )
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.obj("message" -> "connected"))
  }

}
