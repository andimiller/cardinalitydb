package net.andimiller.cardinalitydb

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import scala.util.control.NoStackTrace

object models {
  private implicit val finiteDurationDecoder: Decoder[FiniteDuration] =
    Decoder.decodeString.emapTry { s =>
      Try {
        Duration.apply(s)
      }.flatMap {
        case infinite: Duration.Infinite =>
          Try {
            throw new Exception("Cannot accept infinite durations") with NoStackTrace
          }
        case duration: FiniteDuration => Try { duration }
      }
    }
  private implicit val finiteDurationEncoder: Encoder[FiniteDuration] = Encoder { d =>
    Json.fromString(
      d.toString()
    )
  }
  case class BucketClass(name: String, expiry: Option[FiniteDuration])
  object BucketClass {
    implicit val enc: Encoder[BucketClass] = deriveEncoder
    implicit val dec: Decoder[BucketClass] = deriveDecoder
  }
}
