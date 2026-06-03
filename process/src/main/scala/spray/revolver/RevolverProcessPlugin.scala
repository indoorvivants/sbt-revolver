/*
 * Copyright (C) 2009-2012 Johannes Rudolph and Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.revolver

import sbt._
import sbt.Keys._
import Actions._
import Utilities._
import scala.sys.process.Process
import sbt.internal.util.Terminal
import sbtcompat.PluginCompat._
import xsbti.FileConverter


object RevolverProcessPlugin extends AutoPlugin {

    object autoImport extends ProcessRevolverKeys
    import autoImport._
    import RevolverKeys.*

    lazy val settings = Seq(
      reStart := Def.inputTask{
        implicit val conv: FileConverter = fileConverter.value

        val proj = thisProjectRef.value
        val strm = streams.value
        stopAppWithStreams(streams.value, proj)
        assert(!revolverState.getProcess(proj).exists(_.isRunning), "App is already running even though it was supposed to be stopped")
        val color = "[" + updateStateAndGet(_.takeColor) + "]"
        val logger = new SysoutLogger(reLogTag.value, color, Terminal.console.isAnsiSupported)
        val command = reStartCommand.?.value.getOrElse(sys.error("reStartCommand is not set"))
        val formattedProjectName = formatAppName(proj.project, s"$color")

        colorLogger(strm.log).info(s"[YELLOW]Starting application ${formattedProjectName} (${command.mkString(" ")}) in the background ...")

        val args = reStartArgs.value
        val parsedArgs = startArgsParserNoJvm.parsed
        val env = (reStart / envVars).value
        val cwd = (reStart / baseDirectory).value

        val jpb = new java.lang.ProcessBuilder()
        jpb.directory(cwd)
        jpb.command((command ++ args ++ parsedArgs)*)
        env.foreach {case(k,v ) => jpb.environment.put(k, v)}
        val process = Process(jpb)

        val appProcess = AppProcess(proj, color, logger)(process.run(logger, connectInput = false))
        registerAppProcess(proj, appProcess)
        appProcess
      }.evaluated,
    )

    override def trigger  = noTrigger
    override def projectSettings = settings
}
