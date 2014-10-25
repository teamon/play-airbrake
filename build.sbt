organization := "eu.teamon"

name := "play-airbrake"

version := "0.4.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalaBinaryVersion := "2.11"

scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8")

resolvers ++= Seq(
  Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % Option(System.getenv("PLAY_VERSION")).getOrElse("2.3.5") % "compile",
  "com.typesafe.play" %% "play-ws" % Option(System.getenv("PLAY_VERSION")).getOrElse("2.3.5") % "compile"
)

publishTo := Some(Resolver.file("local-maven", new File("/Users/teamon/code/maven")))
