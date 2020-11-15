package net.andimiller.cardinalitydb

import cats.implicits._
import com.monovore.decline._

object CLI {
  sealed trait Mode
  object Mode {
    case class Spec()            extends Mode
    case class Server(port: Int) extends Mode
  }

  val spec: Command[Mode.Spec] =
    Command("spec", "Output the OpenAPI spec")(Opts.unit.as(Mode.Spec()))
  val server: Command[Mode.Server] = Command("server", "Run in a server mode")(
    Opts
      .option[Int]("port", "p", "Port to bind to")
      .withDefault(8080)
      .map(Mode.Server)
  )

  val cli: Command[Mode] = Command("cardinalitydb", "the cardinality database")(
    Opts.subcommand(spec) orElse Opts.subcommand(server)
  )

}
