name := "cardinalitydb"

version := "0.1"

scalaVersion := "2.12.10"

scalacOptions += "-Ypartial-unification"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// val redis4catsVersion = "0.10.3+51-8f84aaca+20201110-1247-SNAPSHOT"
val redis4catsVersion = "0.0.0+1-f003ef4b-SNAPSHOT"

libraryDependencies ++= List(
  "dev.profunktor"              %% "redis4cats-effects"       % redis4catsVersion,
  "dev.profunktor"              %% "redis4cats-log4cats"      % redis4catsVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core"               % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % "0.17.0-M8",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % "0.17.0-M8",
  "ch.qos.logback"              % "logback-classic"           % "1.2.3",
  "net.logstash.logback"        % "logstash-logback-encoder"  % "6.1",
  "org.slf4j"                   % "slf4j-api"                 % "1.7.28",
  "org.codehaus.janino"         % "janino"                    % "3.1.0" % "runtime",
  "io.chrisdavenport"           %% "log4cats-slf4j"           % "1.0.1"
)
