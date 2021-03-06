organization := "eu.teamon"

name := "play-airbrake"

version := "0.3.1-SNAPSHOT"

scalaVersion := "2.10.0"

scalaBinaryVersion := "2.10"

scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8")

resolvers ++= Seq(
  Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "play" %% "play" % Option(System.getenv("PLAY_VERSION")).getOrElse("2.1.0") % "compile"
)

publishTo := Some(Resolver.file("local-maven", new File("/Users/teamon/code/maven")))
