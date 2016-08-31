import sbt.Keys._

name := "spark-streaming-twitter"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming-twitter" % "1.6.1",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "org.slf4j" % "slf4j-api" % "1.7.2",
  "com.typesafe" % "config" % "1.2.1",
  "com.github.scopt" %% "scopt" % "3.3.0"
)

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") =>
    MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

fork := true
