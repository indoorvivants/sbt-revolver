

val Versions = new {
  val Scala3 = "3.8.3"
  val Scala212 = "2.12.21"
}

lazy val root = project.in(file("."))
  .aggregate(core, jvm, native)
  .aggregate(example.projectRefs*)


val core = project.in(file("core")).enablePlugins(SbtPlugin)
  .settings(
  name := "sbt-revolver-core",
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value
)

val jvm = project.in(file("jvm")).dependsOn(core).enablePlugins(SbtPlugin)
  .settings(
  name := "sbt-revolver",
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value
)

val native = project.in(file("native")).dependsOn(core).enablePlugins(SbtPlugin)
  .settings(
  name := "sbt-revolver-native",
  addSbtPlugin("org.scala-native" % "sbt-scala-native" % nativeVersion),
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value
)

lazy val example = projectMatrix.in(file("example"))
  .nativePlatform(Seq(Versions.Scala3), Seq.empty, _.enablePlugins(RevolverNativePlugin))
  .jvmPlatform(Seq(Versions.Scala3))
  .defaultAxes(VirtualAxis.scalaABIVersion(Versions.Scala3))
  .settings(
    libraryDependencies ++= List(
      "org.http4s" %%% "http4s-ember-server" % "0.23.34",
      "org.http4s" %%% "http4s-dsl" % "0.23.34",
      "org.http4s" %%% "http4s-circe" % "0.23.34",
      "com.outr" %%% "scribe-cats" % "3.19.0",
    ),
    reStartArgs := {
      if(virtualAxes.value.contains(VirtualAxis.jvm)) Seq("8099") else Seq("8011")
    }
  )

addCommandAlias("ci", "scripted")

inThisBuild(
  List(
    organization      := "com.indoorvivants",
    organizationName  := "Anton Sviridov",
    homepage := Some(
      url("https://github.com/indoorvivants/sbt-revolver")
    ),
    startYear := Some(2026),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "keynmol",
        "Anton Sviridov",
        "keynmol@gmail.com",
        url("https://blog.indoorvivants.com")
      ),
      Developer(
        "sbt-revolver-contributors",
        "Sbt Revolver Contributors",
        "",
        url("https://github.com/spray/sbt-revolver/graphs/contributors"))

    )
  )
)
