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


object RevolverNativePlugin extends AutoPlugin {

    object autoImport extends NativeRevolverKeys
    import autoImport._
    import RevolverKeys.*

    private lazy val SN = scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport

    lazy val settings = Seq(
      reStart := Def.inputTask{
        implicit val conv: FileConverter = fileConverter.value

        val proj = thisProjectRef.value
        val strm = streams.value
        stopAppWithStreams(streams.value, proj)
        assert(!revolverState.getProcess(proj).exists(_.isRunning), "App is already running even though it was supposed to be stopped")
        val color = "[" + updateStateAndGet(_.takeColor) + "]"
        val binary = (Compile / SN.nativeLink).value
        val logger = new SysoutLogger(reLogTag.value, color, Terminal.console.isAnsiSupported)

        colorLogger(strm.log).info("[YELLOW]Starting application %s in the background ..." format formatAppName(proj.project, s"$color"))

        val args = reStartArgs.value
        val env = (reStart / envVars).value
        val jpb = new java.lang.ProcessBuilder()
        jpb.command((Seq(toFile(binary).getAbsolutePath()) ++ args)*)
        env.foreach {case(k,v ) => jpb.environment.put(k, v)}
        val process = Process(jpb)

        val appProcess = AppProcess(proj, color, logger)(process.run(logger, connectInput = false))
        registerAppProcess(proj, appProcess)
        appProcess
      }.dependsOn(Compile / SN.nativeLink).evaluated,



      // stop a possibly running application if the project is reloaded and the state is reset
      Global / onUnload ~= { onUnload => state =>
        stopApps(colorLogger(state))
        onUnload(state)
      },

      Global / onLoad := { state =>
        val colorTags = (reStart/ reColors).value.map(_.toUpperCase.formatted("[%s]"))
        GlobalState.update(_.copy(colorPool = collection.immutable.Queue(colorTags *)))
        (Global / onLoad).value.apply(state)
      }
    )

    override def requires = scala.scalanative.sbtplugin.ScalaNativePlugin && spray.revolver.RevolverCorePlugin
    override def trigger  = allRequirements
    override def projectSettings = settings
}
