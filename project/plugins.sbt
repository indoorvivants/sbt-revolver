addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")
addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.11.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")

libraryDependencies ++= List(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
)

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "core" / "src" / "main" / "scala"

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "native" / "src" / "main" / "scala"

Compile / unmanagedSourceDirectories +=
  (ThisBuild / baseDirectory).value.getParentFile /
    "jvm" / "src" / "main" / "scala"
