import org.scalajs.linker.interface.ModuleSplitStyle


scalaVersion := "3.8.3"

libraryDependencies ++= List(
  "com.raquo" %%% "laminar" % "17.2.1"
)

enablePlugins(ScalaJSPlugin)

scalaJSUseMainModuleInitializer := true
reStartCommand := Seq("npm", "run", "dev")
// We need this for the sbt invocation that the Scala.js Vite plugin will run
reStart / envVars ++= Map("JAVA_OPTS" -> s"-Dplugin.version=${System.getProperty("plugin.version")}")

scalaJSLinkerConfig ~= {
  _.withModuleKind(ModuleKind.ESModule)
    .withModuleSplitStyle(
      ModuleSplitStyle.SmallModulesFor(List("app")))
}

enablePlugins(RevolverProcessPlugin)
