// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.1")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.8.4")
resolvers += Resolver.bintrayRepo("playframework", "maven")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % "0.8.2"
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.35")
