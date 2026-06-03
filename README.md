**Note: this project is a fork of the original [sbt-revolver](https://github.com/spray/sbt-revolver), adding support for Scala Native, modernizing the build and publishing. The changes were quite extensive but might eventually end up back upstream**

---

_sbt-revolver_ is a plugin for [SBT] enabling a super-fast development turnaround for your Scala applications.

It sports the following features:

* Starting and stopping your application in the background of your interactive SBT shell
  * This works for both Scala JVM and Scala Native projects (via separate plugin)
* Running custom processes in the background (via separate plugin)


<!--toc:start-->
- [Installation](#installation)
- [Usage](#usage)
  - [Common settings and tasks](#common-settings-and-tasks)
  - [RevolverPlugin - JVM applications](#revolverplugin-jvm-applications)
    - [Hot Reloading](#hot-reloading)
  - [RevolverNativePlugin - Scala Native applications](#revolvernativeplugin-scala-native-applications)
  - [RevolverProcessPlugin - arbitrary processes](#revolverprocessplugin-arbitrary-processes)
- [Triggered Restart](#triggered-restart)
- [License](#license)
- [Patch Policy](#patch-policy)
<!--toc:end-->

## Installation

_sbt-revolver_ supports sbt 1.x and 2.x. Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.indoorvivants" % "sbt-revolver" % "0.11.2")
// for native projects 
addSbtPlugin("com.indoorvivants" % "sbt-revolver-native" % "0.11.2")
// for launching any process in the background
addSbtPlugin("com.indoorvivants" % "sbt-revolver-process" % "0.11.2")
```


## Usage

*sbt-revolver* provides three plugins (in separate dependencies):

- `RevolverPlugin` – launching JVM applications

- `RevolverNativePlugin` – launching Scala Native applications

- `RevolverProcessPlugin` – launching any process in the background (for example, for Scala.js projects one can launch Vite's dev server in the background)

### Common settings and tasks

All plugins share a common set of tasks and settings

Tasks:

* `reStart` starts the process (if one is already running, it's killed)
* `reStop` stops application
* `reStatus` shows an informational message about the current running state of the application.

Settings:

* `reStartArgs`, a `SettingKey[Seq[String]]`, which lets you define arguments that are passed to your
  application on every start. Any arguments given to the `reStart` task directly will be appended to this setting.

* `reStart / baseDirectory`, a `SettingKey[File]`, which lets you customize the base directory independently from
  what `run` assumes.

* `reStart / envVars`, which lets you customize the environment variables for running the application

* `reColors`, a `SettingKey[Seq[String]]`, which lets you change colors used to tag output from running processes.
  There are some pre-defined color schemes, see the example section below.

* `reLogTag`, a `SettingKey[String]`, which lets you change the log tag shown in front of log messages. Default is the
  project name.

Examples:

- To set fixed start arguments (than you can still append to with the `reStart` task):

    `reStartArgs := Seq("-x")`


- To change set of colors used to tag output from multiple processes:

    `reColors := Seq("blue", "green", "magenta")`

    There are predefined color schemes to use with `reColors`: `Revolver.noColors`, `Revolver.basicColors`,
`Revolver.basicColorsAndUnderlined`.

- To add environment variables when running the application:

    `reStart / envVars := Map("USER_TOKEN" -> "2359298356239")`


### RevolverPlugin - JVM applications

- **Installation**: `addSbtPlugin("com.indoorvivants" % "sbt-revolver" % "0.11.2")` in your `project/plugins.sbt`
- **Enabling**: auto-enabled for all JVM projects
- **Quick start**: run `reStart` on the project

Tasks:

* `reStart <args> --- <jvmArgs>` starts your application in a forked JVM.
  The optionally specified (JVM) arguments are appended to the ones configured via the `reStartArgs`/
  `reStart::javaOptions` setting (see the "Configuration" section below). If the application is already running it
  is first stopped before being restarted.

* `reStop` stops application. This is done by simply force-killing the forked JVM. Note, that this means that [shutdown hooks] are not run (see
  [#20](https://github.com/spray/sbt-revolver/issues/20))


Settings:

* `reStart / mainClass`, which lets you optionally define a  main class to run in `reStart` independently of the
  one set for running the project normally. This value defaults to the value of `compile:run::mainClass`. If you
  don't specify a value here explicitly the same logic as for the normal run main class applies: If only one main class
  is found it one is chosen. Otherwise, the main-class chooser is shown to the user.
* `reStart / javaOptions`, a `SettingKey[Seq[String]]`, which lets you define the options to pass to the forked JVM
  when starting your application
* `reStart / fullClasspath`, which lets you customize the full classpath path for running with `reStart`.
* `reJrebelJar`, a `SettingKey[String]`, which lets you override the value of the `JREBEL_PATH` env variable.
* `debugSettings`, a `SettingKey[Option[DebugSettings]]` to specify remote debugger settings. There's a convenience
  helper `Revolver.enableDebugging` to simplify to enable debugging (see examples).


Examples:

- To configure a 2 GB memory limit for your app when started with `reStart`:

    `reStart / javaOptions += "-Xmx2g"`

- To set a special main class for your app when started with `reStart`:

    `reStart / mainClass := Some("com.example.Main")`

- To enable debugging with the specified options:

    `Revolver.enableDebugging(port = 5050, suspend = true)`

#### Hot Reloading

*Note: JRebel support in sbt-revolver is not actively supported any more.*

If you have JRebel installed you can let _sbt-revolver_ know where to find the `jrebel.jar`. You can do this
either via the `Revolver.jRebelJar` setting directly in your SBT config or via a shell environment variable with the
name `JREBEL_PATH` (which is the recommended way, since it doesn't pollute your SBT config with system-specific settings).
For example, on OSX you would add the following line to your shell startup script:

    export JREBEL_PATH=/Applications/ZeroTurnaround/JRebel/jrebel.jar

With JRebel _sbt-revolver_ supports hot reloading:

* Start your application with `reStart`.
* Enter "triggered compilation" with `~products`. SBT watches for changes in your source (and resource) files.
  If a change is detected SBT recompiles the required classes and JRebel loads these classes right into your running
  application. Since your application is not restarted the time required to bring changes online is minimal (see
  the "Understanding JRebel" section below for more details). When you press &lt;ENTER&gt; SBT leaves triggered compilation
  and returns to the normal prompt keeping your application running.
* If you changed your application in a way that requires a full restart (see below) press &lt;ENTER&gt; to leave
  triggered compilation and `reStart`.
* Of course you always stop the application with `reStop`.


### RevolverNativePlugin - Scala Native applications

- **Installation**: `addSbtPlugin("com.indoorvivants" % "sbt-revolver-native" % "0.11.2")` in your `project/plugins.sbt`
- **Enabling**: auto-enabled for all Scala Native projects, disable it with `disablePlugins(RevolverNativePlugin)`
- **Quick start**: run `reStart` on the project

The plugin does not define any extra tasks/settings beyond the common ones.

### RevolverProcessPlugin - arbitrary processes

- **Installation**: `addSbtPlugin("com.indoorvivants" % "sbt-revolver-process" % "0.11.2")` in your `project/plugins.sbt`
- **Enabling**: needs to be explicitly enabled with `enablePlugins(RevolverProcessPlugin)`
- **Quick start**: configure `reStartCommand` on the project and run it with `reStart`

Settings:

* `reStartCommand` is a `Setting[Seq[String]]` that specified the command that will be run. This setting is required - `reStart` will fail if `reStartCommand` is not configured.

Examples:

* If you are using Vite's development server with your Scala.js application, then `reStartCommand := Seq("npm", "run", "build")` will allow you 
  to run it in the background (leaving the SBT shell free for running `~fastLinkJS` for fast iteration) 


## Triggered Restart

You can use `~reStart` to go into "triggered restart" mode. Your application starts up and SBT watches for changes in
your source (or resource) files. If a change is detected SBT recompiles the required classes and _sbt-revolver_
automatically restarts your application.

When you press &lt;ENTER&gt; SBT leaves "triggered restart" and returns to the normal prompt keeping your application running.

To customize which files should be watched for triggered restart see the sbt documentation about [Triggered Execution](http://www.scala-sbt.org/0.13/docs/Triggered-Execution.html).


## License

_sbt-revolver_ is licensed under [APL 2.0].


## Patch Policy

Feedback and contributions to the project, no matter what kind, are always very welcome.
However, patches can only be accepted from their original author.
Along with any patches, please state that the patch is your original work and that you license the work to the
_sbt-revolver_ project under the project’s open source license.


  [SBT]: https://github.com/harrah/xsbt/wiki
  [JRebel]: http://zeroturnaround.com/software/jrebel/
  [xsbt-web-plugin]: https://github.com/aolshevskiy/xsbt-web-plugin
  [spray]: http://spray.io
  [spray-can]: https://github.com/spray/spray-can
  [shutdown hooks]: http://docs.oracle.com/javase/6/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)
  [JRebel FAQ]: http://zeroturnaround.com/software/jrebel/learn/faq/
  [APL 2.0]: http://www.apache.org/licenses/LICENSE-2.0
