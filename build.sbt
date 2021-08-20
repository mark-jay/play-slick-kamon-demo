name := """play-slick-kamon-demo"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, JavaAgent)
val AkkaVersion = "2.6.14"

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// https://mvnrepository.com/artifact/de.siegmar/logback-gelf
libraryDependencies += "de.siegmar" % "logback-gelf" % "3.0.0"

libraryDependencies += "io.kamon" %% "kamon-bundle" % "2.1.0" % Compile
libraryDependencies += "io.kamon" %% "kamon-prometheus" % "2.1.0" % Compile

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.2"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.23"

// test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test"
//libraryDependencies += "org.specs2" %% "specs2-core" % "4.2.0" % "test"

// https://mvnrepository.com/artifact/org.testcontainers/postgresql
libraryDependencies += "org.testcontainers" % "testcontainers" % "1.16.0" % Test
libraryDependencies += "org.testcontainers" % "postgresql" % "1.16.0" % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
