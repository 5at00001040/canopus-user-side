name := """canopus-user-side"""
organization := "com.canopus"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// GCP
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "1.14.0"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.canopus.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.canopus.binders._"
