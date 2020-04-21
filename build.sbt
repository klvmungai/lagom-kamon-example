import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator

organization in ThisBuild := "org.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.0"

enablePlugins(DockerComposePlugin)

dockerImageCreationTask := {
  Seq((Docker / publishLocal in `hello-impl`).value)
}

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
val kanelaAgent = "io.kamon" % "kanela-agent" % "1.0.5"

lazy val `hello` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

val akkaVersion = "2.6.4"

lazy val akkaDep = Seq(
  dependencyOverrides ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-coordination" % akkaVersion,
    "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
    "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  )
)

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala, AkkaGrpcPlugin, DockerPlugin, JavaAgent, JavaAppPackaging, PlayAkkaHttp2Support)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      "io.kamon" %% "kamon-bundle" % "2.1.0",
      "io.kamon" %% "kamon-jaeger" % "2.1.0",
      "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % "1.6.1",
      "com.lightbend.play"      %% "play-grpc-runtime"   % "0.8.2",
      lagomLogback
    )
  )
  .settings(
    name := "hello",
    dockerExposedPorts += 9000,
    javaAgents += kanelaAgent,
    dockerBaseImage := "openjdk:11",
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server),
    akkaGrpcExtraGenerators in Compile ++= Seq(PlayScalaServerCodeGenerator),
    akkaGrpcCodeGeneratorSettings += "server_power_apis",
    akkaDep
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`hello-api`)
