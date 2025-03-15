name         := """scala-react-chat-app"""
organization := "com.reidoboss"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.4"

lazy val PekkoVersion = "1.0.3"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play"           % "7.0.1" % Test,
  "org.apache.pekko"       %% "pekko-cluster-sharding-typed" % PekkoVersion
)
