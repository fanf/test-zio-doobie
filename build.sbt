name := "test-zio"

version := "0.1"

scalaVersion := "2.13.1"


libraryDependencies ++= List(
  "dev.zio"              %% "zio"              % "1.0.3",
  "dev.zio"              %% "zio-test"         % "1.0.3",
  "dev.zio"              %% "zio-streams"      % "1.0.3",
  "dev.zio"              %% "zio-interop-cats" % "2.2.0.1",
  "org.tpolecat"         %% "doobie-core"      % "0.9.2",
  "org.tpolecat"         %% "doobie-postgres"  % "0.9.2",
  "com.github.pathikrit" %% "better-files"     % "3.8.0",

  "commons-io"     % "commons-io"      % "2.6",
  "joda-time"      % "joda-time"       % "2.10.5",
  "com.zaxxer"     % "HikariCP"        % "3.4.5",
  "org.postgresql" % "postgresql"      % "42.2.18",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
