scalaVersion := "3.8.3"

libraryDependencies ++= List(
  "org.http4s" %% "http4s-ember-server" % "0.23.34",
  "org.http4s" %% "http4s-dsl" % "0.23.34",
  "org.http4s" %% "http4s-circe" % "0.23.34",
  "com.outr" %% "scribe-cats" % "3.19.0",
)

enablePlugins(RevolverPlugin)

reStart / envVars += "TEST_VAR" -> "OK"
