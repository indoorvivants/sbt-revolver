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

object RevolverCorePlugin extends AutoPlugin {

    object autoImport extends RevolverKeys {
      object Revolver {

        def noColors: Seq[String] = Nil
        def basicColors = Seq("BLUE", "MAGENTA", "CYAN", "YELLOW", "GREEN")
        def basicColorsAndUnderlined = basicColors ++ basicColors.map("_"+_)
      }
    }

    import autoImport.*

    lazy val settings = Seq(
      reStart / reColors := Revolver.basicColors,
      reStop := stopAppWithStreams(streams.value, thisProjectRef.value),

      reStatus := showStatus(streams.value, thisProjectRef.value),

      // default: no arguments to the app
      reStartArgs in Global := Seq.empty,
      reLogTagUnscoped := thisProjectRef.value.project,

      // stop a possibly running application if the project is reloaded and the state is reset
      Global / onUnload  ~= { onUnload => state =>
        stopApps(colorLogger(state))
        onUnload(state)
      },

      Global / onLoad  := { state =>
        val colorTags = (reStart / reColors).value.map(_.toUpperCase formatted "[%s]")
        GlobalState.update(_.copy(colorPool = collection.immutable.Queue(colorTags: _*)))
        (onLoad in Global).value.apply(state)
      }
    )

    override def requires = Plugins.empty
    override def trigger  = allRequirements
    override def projectSettings = settings
}
