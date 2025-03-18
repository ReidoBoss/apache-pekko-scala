name         := """check-me-api"""
organization := "com.vauldex"

version := "0.4.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, AshScriptPlugin, DockerPlugin)

import java.io.File

import com.typesafe.config._

val conf = settingKey[Config]("Parsed configuration object")
conf := ConfigFactory
  .parseFile(
    new File(sys.env.getOrElse("CONFIG_FILE", "conf/application.conf"))
  )
  .resolve()

scalaVersion := "3.3.1"

lazy val pekkoVersion = "1.2.0-M1"

libraryDependencies ++= Seq(
  guice,
  caffeine,
  "org.scalatestplus.play" %% "scalatestplus-play"           % "7.0.1" % Test,
  "org.apache.pekko"       %% "pekko-cluster-sharding-typed" % pekkoVersion,
  "org.apache.pekko"       %% "pekko-serialization-jackson"  % pekkoVersion,
  "org.apache.pekko"       %% "pekko-persistence-typed"      % pekkoVersion
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.vauldex.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.vauldex.binders._"

play.sbt.routes.RoutesKeys.routesImport += "java.util.UUID"

dockerExposedPorts ++= Seq(9000, 25520)
dockerBaseImage := "amazoncorretto:21-alpine3.18-jdk"
maintainer      := "tech.vauldex"

Docker / daemonUserUid := None
Docker / daemonUser    := "daemon"
Universal / javaOptions ++= Seq(
  "-J-Xmx1024m",
  "-J-Xmx786m",
  "-Dpidfile.path=/dev/null",
  "-Dconfig.override_with_env_vars=true",
  "-Dwebsocket.frame.maxLength=64k"
)

generateReverseRouter := true
