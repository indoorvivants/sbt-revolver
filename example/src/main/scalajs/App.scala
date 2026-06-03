package app

import com.raquo.laminar.api.L.*
import org.scalajs.dom

@main def hello =
    lazy val appContainer = dom.document.querySelector("#appContainer")
    val appElement = div(h1("Hello world"), ul(Seq.tabulate(10)(i => li(i.toString))))
    renderOnDomContentLoaded(appContainer, appElement)
