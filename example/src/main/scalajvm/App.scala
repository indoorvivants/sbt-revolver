import io.circe.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import cats.effect.*
import scala.concurrent.duration.*
import com.comcast.ip4s.*

case class Resp(x: String) derives Codec.AsObject

object Main extends IOApp:
  val log = scribe.cats.io
  val routes = HttpRoutes.of[IO] { case GET -> Root / "hello" / name =>
    Ok(Resp(name))
  }
  override def run(args: List[String]) =
    val port = args.headOption.flatMap(Port.fromString).getOrElse(port"8888")
    EmberServerBuilder
      .default[IO]
      .withPort(port)
      .withHttpApp(routes.orNotFound)
      .withShutdownTimeout(1.second)
      .build
      .evalTap(server => log.info(s"Server started on ${server.baseUri}"))
      // .use(_ => IO.println("Press Enter to stop") *> IO.readLine)
      .useForever
      .void
      .as(ExitCode.Success)
end Main
