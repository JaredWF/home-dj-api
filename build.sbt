import NativePackagerKeys._

packageArchetype.java_application

name := "dj-station"

version := "0.1"

scalaVersion := "2.10.3"

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0")

libraryDependencies ++= Seq(  
	"io.spray" % "spray-routing" % "1.2.0",
  "io.spray" % "spray-can" % "1.2.0",
  "io.spray" %%  "spray-json" % "1.2.5",
  "com.mashape.unirest" % "unirest-java" % "1.4.7",
	"com.typesafe.akka" % "akka-actor_2.10" % "2.2.4",
	"net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)

Revolver.settings