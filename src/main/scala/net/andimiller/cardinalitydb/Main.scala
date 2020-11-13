package net.andimiller.cardinalitydb

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.Redis
import net.andimiller.cardinalitydb.models.BucketClass
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  implicit val logger: Logger[IO] = Slf4jLogger.getLoggerFromClass(getClass)

  override def run(args: List[String]): IO[ExitCode] =
    Redis[IO].utf8("redis://localhost").use { cmd =>
      for {
        _ <- IO {
              println("starting")
            }
        routes = new Routes[IO](
          List(
            BucketClass("main", 10.minutes.some)
          ),
          cmd)
        e <- routes.serve(global).serve.compile.lastOrError
      } yield e
    }
}
