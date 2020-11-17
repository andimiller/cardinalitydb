package net.andimiller.cardinalitydb

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import scala.util.control.NoStackTrace
import cats.implicits._
import sttp.tapir._
import sttp.tapir.generic.Derived
import sttp.tapir.generic._

object models {
  private def readFiniteDuration(s: String) =
    Try { Duration(s) }.toEither
      .leftMap(_.getMessage)
      .flatMap {
        case _: Duration.Infinite => "Cannot accept infinite durations".asLeft
        case d: FiniteDuration    => d.asRight
      }

  implicit val finiteDurationSchema: Schema[FiniteDuration] = Schema(SchemaType.SString)

  private implicit val finiteDurationDecoder: Decoder[FiniteDuration] =
    Decoder.decodeString.emap(readFiniteDuration)
  private implicit val finiteDurationEncoder: Encoder[FiniteDuration] =
    Encoder.encodeString.contramap(_.toString)
  case class BucketClass(
      @description("The name of this bucket") name: String,
      @description("The TTL of items in this bucket") expiry: Option[FiniteDuration]
  )
  object BucketClass {
    implicit val enc: Encoder[BucketClass]   = deriveEncoder
    implicit val dec: Decoder[BucketClass]   = deriveDecoder
    implicit val schema: Schema[BucketClass] = Schema.derivedSchema[BucketClass]
  }
}
