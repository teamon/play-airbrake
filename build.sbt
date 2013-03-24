organization := "eu.teamon"

name := "play-airbrake"

version := "0.3.1-SNAPSHOT"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8")

resolvers ++= Seq(
  Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "play" % "play_2.10" % Option(System.getenv("PLAY_VERSION")).getOrElse("2.1.0") % "compile"
)

seq(scalajarsSettings:_*)

scalajarsProjectName := "play-airbrake"

