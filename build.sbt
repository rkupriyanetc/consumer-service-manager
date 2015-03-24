name := """consumer-service-manager"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.2"

resolvers ++= Seq(
  Resolver.url("Objectify Play Repository", url("http://deadbolt.ws/releases/"))(Resolver.ivyStylePatterns),
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/"
)

libraryDependencies ++= Seq(
  javaCore,
  cache,
  "be.objectify"         %% "deadbolt-java"           % "2.3.2",
  "com.feth"             %% "play-authenticate"       % "0.6.8",
  "org.mongodb"          %  "mongo-java-driver"       % "2.13.0",
  "org.apache.poi"       %  "poi"                     % "3.11",
  "org.apache.poi"       %  "poi-ooxml"               % "3.11"
)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)