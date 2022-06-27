// sbt new scala/scala3.g8

val scala3Version = "3.1.2"

val AkkaVersion = "2.6.18"
val AkkaHttpVersion = "10.2.9"
val PprintVersion = "0.7.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "chat-server",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,

      ("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion).cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-stream" % AkkaVersion).cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13),
      ("com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13),

      "com.lihaoyi" %% "pprint" % PprintVersion
    )
  )
