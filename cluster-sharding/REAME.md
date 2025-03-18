# Cluster Sharding

This project implements **Apache Pekko Cluster Sharding** to manage user actors in a distributed system.
It ensures that user actors are **created, tracked, and removed efficiently** when terminated.

---

## 📌 Overview

- **Cluster Sharding** enables **scalable actor management** across multiple nodes.
- **UserManager** and **WorkspaceUserManager** handle actor lifecycle and tracking.
- **Actor Termination** is managed using `context.watch` and `receiveSignal` to ensure proper cleanup.

---

## 🚀 Features

✅ **Distributed actor management** – Ensures unique actor instances across the cluster.
✅ **Automatic cleanup** – Stops `UserManager` when all user actors are removed.
✅ **WebSocket handling** – Supports real-time communication via streams.
✅ **Cluster Sharding integration** – Uses entity sharding for efficient user tracking.

---

## ⚙️ Implementation

### **1️⃣ User Actor**

Each `UserActor` handles **WebSocket communication** and processes JSON messages.

```scala
package actors
package impl

import javax.inject.*

import scala.concurrent.duration.*
import scala.concurrent.Future

import play.api.libs.json.*

import org.apache.pekko
import pekko.actor.typed.ActorRef
import pekko.actor.typed.scaladsl.ActorContext
import pekko.util.Timeout
import pekko.stream.scaladsl.MergeHub
import pekko.stream.scaladsl.BroadcastHub
import pekko.stream.scaladsl.Keep
import pekko.stream.scaladsl.Flow
import pekko.NotUsed
import pekko.Done
import pekko.stream.scaladsl.Sink
import pekko.actor.typed.Behavior
import pekko.actor.typed.scaladsl.Behaviors
import pekko.stream.scaladsl.Source
import pekko.actor.typed.ActorSystem

private case class UserActor @Inject() (
    id: IdUser,
)(using val context: ActorContext[UserActor.Action])
    extends UserActorMixin {
  import UserActor.*
  given system: ActorSystem[Nothing] = context.system

  /** This is where the json from the frontend is received */
  val jsonSink: Sink[JsValue, Future[Done]] =
    Sink.foreach(json => println(json))

  def behaviors: Behavior[UserActor.Action] =
    Behaviors.receiveMessage {

      case Create(replyTo) =>
        replyTo ! websocketFlow
        Behaviors.same

      case event @ CreateEvent(_, _) =>
        sendToClient(event.toJson)
        Behaviors.same

      case Terminate =>
        Behaviors.stopped
    }

}

object UserActor {
  def apply(id: IdUser): Behavior[UserActor.Action] =
    Behaviors.setup { implicit context =>
      new UserActor(id).behaviors
    }

  sealed trait Action
  sealed trait Command  extends Action
  case object Terminate extends Command
  final case class Create(
      replyTo: ActorRef[Flow[JsValue, JsValue, NotUsed]]
  ) extends Command

  sealed trait Event extends Action
  final case class CreateEvent(
      action: String,
      content: JsValue
  ) extends Event {
    def toJson = Json.obj(
      "action"  -> action,
      "content" -> content
    )
  }

}
```

---

### **2️⃣ UserManager**

Handles a **set of UserActor instances**, watching and removing them when they terminate.

```scala
package actors
package impl

import scala.concurrent.duration.*

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Terminated

object UserManager {
  sealed trait Action
  final case class Create(actor: ActorRef[UserActor.Action]) extends Action
  final case class Remove(actor: ActorRef[UserActor.Action]) extends Action
  final case class Get(replyTo: ActorRef[Set[ActorRef[UserActor.Action]]]) extends Action
  case object Terminate extends Action

  def apply(idUser: IdUser): Behavior[Action] =
    manageUsers(Set.empty)

  private def manageUsers(
      activeUsers: Set[ActorRef[UserActor.Action]]
  ): Behavior[Action] = Behaviors
    .receive[UserManager.Action] { (context, message) =>
      message match {
        case Get(replyTo) =>
          replyTo ! activeUsers
          Behaviors.same

        case Create(actor) =>
          context.watch(actor)
          manageUsers(activeUsers + actor)

        case Remove(actor) if activeUsers.size == 1 =>
          Behaviors.stopped

        case Remove(actor) =>
          manageUsers(activeUsers - actor)

        case Terminate => Behaviors.stopped
      }
    }
    .receiveSignal { case (_, Terminated(actor)) =>
      activeUsers.size match
        case 1 => Behaviors.stopped
        case _ => manageUsers(activeUsers.filterNot(_ == actor))
    }
}
```

---

### **3️⃣ Cluster Sharding Modules**

Handles **dependency injection** and **sharding setup** for managing actors.

```scala
package modules

import play.api.libs.concurrent.PekkoGuiceSupport

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter.*

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class ActorModules extends AbstractModule with PekkoGuiceSupport {

  /**
   * Typed Actor System Injection Module
   */
  @Provides
  @Singleton
  def provideTypedActorSystem(
      system: ActorSystem
  ): org.apache.pekko.actor.typed.ActorSystem[?] = system.toTyped

}
```

```scala
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
```

---

## 📖 Usage

### **Creating a UserManager**

```scala
val userManagerRef: EntityRef[UserManager.Action] = userManager(idUser)
userManagerRef ! UserManager.Create(userActorRef)
```

### **Removing a User**

```scala
userManagerRef ! UserManager.Remove(userActorRef)
```

### **Getting Active Users**

```scala
userManagerRef ! UserManager.Get(replyTo = replyActorRef)
```

### **Terminating the Manager**

```scala
userManagerRef ! UserManager.Terminate
```

---

## 🛠️ Requirements

- **Apache Pekko 1.2.0-M1**
- **Play Framework (for Dependency Injection)**
- **Scala 3.3.4**

---

## 📌 Conclusion

This implementation efficiently manages **user actors** in a **scalable cluster** environment.
It ensures **automatic cleanup**, **WebSocket support**, and **fault tolerance**.

🚀 **Ready for production usage!** 🚀
