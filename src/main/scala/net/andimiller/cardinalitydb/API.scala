package net.andimiller.cardinalitydb

import cats.implicits._
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.auto._
import net.andimiller.cardinalitydb.models.BucketClass
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.DecodeResult.{Missing, Value}
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.Info
import sttp.tapir.openapi.circe.yaml._
import io.circe.generic.semiauto._

import scala.concurrent.duration._

object API {
  private lazy val info = Info(
    "CardinalityDB: the cardinality database service",
    "1.0"
  )

  lazy val openApiYaml: String =
    List(
      listBucketClasses,
      getBucket,
      putIds,
      getCardinality,
      clearCardinality
    ).toOpenAPI(info).toYaml

  lazy val listBucketClasses: Endpoint[Unit, Unit, List[BucketClass], Any] =
    endpoint.get
      .name("listBucketClasses")
      .in("db")
      .out(jsonBody[List[BucketClass]])
      .description("List all Bucket Classes configured in this service")

  case class Error(error: String)
  object Error {
    implicit val dec: Decoder[Error] = deriveDecoder
    implicit val enc: Encoder[Error] = deriveEncoder
  }

  case class Message(message: String)
  object Message {
    implicit val dec: Decoder[Message] = deriveDecoder
    implicit val enc: Encoder[Message] = deriveEncoder
  }

  val NotFound = Error("Not Found")

  lazy val getBucket: Endpoint[String, Error, BucketClass, Any] =
    endpoint.get
      .name("getBucketClass")
      .in("db")
      .in(path[String]("bucketClass").example("main"))
      .errorOut(
        oneOf(
          statusMapping(
            StatusCode.NotFound,
            jsonBody[Error]
              .description("not found")
          )
        )
      )
      .out(
        oneOf(
          statusMapping(
            StatusCode.Ok,
            jsonBody[BucketClass]
              .example(BucketClass("main", 10.minutes.some))
              .description("the bucket class queried")
          )
        )
      )
      .description("Get details on a specific Bucket Class")

  lazy val putIds: Endpoint[(String, String, List[String]), Error, Long, Any] =
    endpoint.post
      .name("insertItems")
      .in("db")
      .in(path[String]("bucketClass"))
      .in(path[String]("bucketName"))
      .in(jsonBody[List[String]])
      .out(jsonBody[Long].description("Count of the cardinality of the bucket"))
      .errorOut(
        oneOf(
          statusMapping(
            StatusCode.NotFound,
            jsonBody[Error]
          )
        )
      )
      .description("Add some IDs to a bucket and get the new estimated cardinality")

  lazy val getCardinality: Endpoint[(String, String), Error, Long, Any] =
    endpoint.get
      .name("getCardinality")
      .in("db")
      .in(path[String]("bucketClass"))
      .in(path[String]("bucketName"))
      .out(jsonBody[Long])
      .errorOut(
        oneOf(
          statusMapping(
            StatusCode.NotFound,
            jsonBody[Error]
          )
        )
      )
      .description("Get the estimated cardinality of a bucket")

  lazy val clearCardinality: Endpoint[(String, String), Error, Message, Any] =
    endpoint.delete
      .name("clearCardinality")
      .in("db")
      .in(path[String]("bucketClass"))
      .in(path[String]("bucketName"))
      .out(jsonBody[Message])
      .errorOut(
        oneOf(
          statusMapping(
            StatusCode.NotFound,
            jsonBody[Error]
          )
        )
      )
      .description("Clear a cardinality bucket")
}
