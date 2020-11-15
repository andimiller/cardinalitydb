package net.andimiller.cardinalitydb

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import scala.util.control.NoStackTrace
import cats.implicits._

object models {
  private def readFiniteDuration(s: String) =
    Try { Duration(s) }.toEither
      .leftMap(_.getMessage)
      .flatMap {
        case _: Duration.Infinite => "Cannot accept infinite durations".asLeft
        case d: FiniteDuration    => d.asRight
      }

  private implicit val finiteDurationDecoder: Decoder[FiniteDuration] =
    Decoder.decodeString.emap(readFiniteDuration)
  private implicit val finiteDurationEncoder: Encoder[FiniteDuration] =
    Encoder.encodeString.contramap(_.toString)
  case class BucketClass(name: String, expiry: Option[FiniteDuration])
  object BucketClass {
    implicit val enc: Encoder[BucketClass] = deriveEncoder
    implicit val dec: Decoder[BucketClass] = deriveDecoder
  }
}
