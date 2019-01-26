name := "Diff Service"
version := "0.1"

scalaVersion := "2.12.6"

val vConfig               = "1.3.2"
val vJson4s               = "3.5.3"
val vScalatest            = "3.0.4"
val vAkka                 = "2.5.15"
val vAkkaHttp             = "10.1.4"
val vSlf4J                = "1.7.12"
val vLogback              = "1.2.1"

libraryDependencies ++= Seq(
  "com.typesafe"              % "config"                      % vConfig,

  "org.json4s"                %% "json4s-jackson"             % vJson4s,

  // --- Akka ---
  "com.typesafe.akka"         %% "akka-actor"                 % vAkka,
  "com.typesafe.akka"         %% "akka-cluster"               % vAkka,
  "com.typesafe.akka"         %% "akka-cluster-tools"         % vAkka,
  "com.typesafe.akka"         %% "akka-slf4j"                 % vAkka
    exclude ("org.slf4j", "slf4j-api")
    exclude("com.typesafe.akka", "akka-actor_2.12"),
  "com.typesafe.akka"         %% "akka-http"                  % vAkkaHttp
    exclude ("com.typesafe.akka", "akka-stream_2.12")
    exclude ("com.typesafe.akka", "akka-actor_2.12"),
  "com.typesafe.akka"         %% "akka-remote"                % vAkka
    exclude ("com.typesafe.akka", "akka-stream_2.12"),

  // --- Testing ---
  "org.scalatest"             %% "scalatest"                  % vScalatest % Test,
  "com.typesafe.akka"         %% "akka-testkit"               % vAkka % Test,
  "com.typesafe.akka"         %% "akka-http-testkit"          % vAkkaHttp % Test,

  // --- Log ---
  "org.slf4j"                 %  "slf4j-api"                  % vSlf4J,
  "org.slf4j"                 %  "log4j-over-slf4j"           % vSlf4J,
  "ch.qos.logback"            %  "logback-classic"            % vLogback
    exclude("org.slf4j", "slf4j-api")

)
