organization := "eu.teamon"

name := "play-airbrake"

version := "0.3.1-SNAPSHOT"

scalaVersion := "2.12.1"

scalaBinaryVersion := "2.12"

scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8")

resolvers ++= Seq(
  Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  	"com.typesafe.play" %% "play" % Option(System.getenv("PLAY_VERSION")).getOrElse("2.6.1") % "compile",
	"com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2",
	"com.typesafe.play" %% "play-ws-standalone-json" % "1.1.2",
	"com.typesafe.play" %% "play-ws-standalone-xml" % "1.1.2"
)

publishTo := Some(Resolver.file("local-maven", new File(Option(System.getenv("MAVEN_REPO")).getOrElse("~/.m2/repository"))))


