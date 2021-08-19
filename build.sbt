name := """play-slick-kamon-demo"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, JavaAgent)

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// https://mvnrepository.com/artifact/de.siegmar/logback-gelf
libraryDependencies += "de.siegmar" % "logback-gelf" % "3.0.0"

libraryDependencies += "io.kamon" %% "kamon-bundle" % "2.1.0" % Compile
libraryDependencies += "io.kamon" %% "kamon-prometheus" % "2.1.0" % Compile

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
