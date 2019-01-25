name := "mapreduce-akka"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.19"


libraryDependencies ++= Seq(
  // akka core
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  //  akka server
  "com.typesafe.akka" %% "akka-http"   % "10.1.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  // test
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
)

