name := "consumer-service-manager"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaCore,
  cache,
  javaWs,
  "be.objectify"              %% "deadbolt-java"       % "2.4.3",
  "com.feth"                  %% "play-authenticate"   % "0.7.0",
  "org.mongodb"               %  "mongo-java-driver"   % "3.0.4",
  "org.apache.poi"            %  "poi"                 % "3.12",
  "org.apache.poi"            %  "poi-ooxml"           % "3.12",
  "org.webjars"               %  "bootstrap"           % "3.3.5",
  "org.webjars"               %  "select2"             % "4.0.0-2",
  "org.easytesting"           %  "fest-assert"         % "1.4"              % "test"
)

// add resolver for deadbolt and easymail snapshots
resolvers ++= Seq( 
  Resolver.sonatypeRepo("snapshots")
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

lazy val root = (project in file(".")).enablePlugins(PlayJava)

//  Uncomment the next line for local development of the Play Authenticate core:
//lazy val playAuthenticate = project.in(file("modules/play-authenticate")).enablePlugins(PlayJava)

//routesGenerator := InjectedRoutesGenerator

EclipseKeys.preTasks := Seq(compile in Compile)

