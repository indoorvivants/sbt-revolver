

val Versions = new {
  val Scala3 = "3.8.3"
  val Scala212 = "2.12.21"
}

lazy val root = project.in(file("."))
  .aggregate(core.projectRefs*)
  .aggregate(jvm.projectRefs*)
  .aggregate(native.projectRefs*)
  .aggregate(example.projectRefs*)
  .settings(
    publish / skip := true,
    publishLocal /skip := true
  )


val core = projectMatrix
  .in(file("core"))
  .enablePlugins(SbtPlugin)
  .jvmPlatform(Seq(Versions.Scala3, Versions.Scala212))
  .settings(
  name := "sbt-revolver-core",
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value,
  addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
  pluginCrossBuild / sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.12" => "1.12.11"
      case _      => "2.0.0-RC14"
    }
  }
)

val jvm = projectMatrix.in(file("jvm"))
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .jvmPlatform(Seq(Versions.Scala3, Versions.Scala212))
  .settings(
  name := "sbt-revolver",
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value,
  addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
  pluginCrossBuild / sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.12" => "1.12.11"
      case _      => "2.0.0-RC14"
    }
  }
)

val native = projectMatrix.in(file("native"))
  .dependsOn(core)
  .enablePlugins(SbtPlugin)
  .jvmPlatform(Seq(Versions.Scala3, Versions.Scala212))
  .settings(
  name := "sbt-revolver-native",
  addSbtPlugin("org.scala-native" % "sbt-scala-native" % nativeVersion),
  scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false,
  Test / test := (Test / test).dependsOn(scripted.toTask("")).value,
  addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
  sbtTestDirectory := {
    scalaBinaryVersion.value match {
      case "2.12" => (sourceDirectory).value / "sbt-test"
      case _      => (sourceDirectory).value / "sbt-test-sbt2"
    }
  },
  pluginCrossBuild / sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.12" => "1.12.11"
      case _      => "2.0.0-RC14"
    }
  }
)

lazy val example = projectMatrix.in(file("example"))
  .nativePlatform(Seq(Versions.Scala3), Seq.empty, _.enablePlugins(RevolverNativePlugin))
  .jvmPlatform(Seq(Versions.Scala3))
  .defaultAxes(VirtualAxis.scalaABIVersion(Versions.Scala3))
  .settings(
    publish / skip := true,
    publishLocal / skip := true,
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
