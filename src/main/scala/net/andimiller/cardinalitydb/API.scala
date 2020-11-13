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

import scala.concurrent.duration.FiniteDuration

object API {
  implicit val finiteDurationSchema: Schema[FiniteDuration] = Schema(SchemaType.SString)

  private lazy val info = Info(
    "CardinalityDB: the cardinality database service",
    "1.0"
  )

  lazy val openApiYaml: String =
    List(
      listBucketClasses,
      getBucket,
      putIds,
      getCardinality
    ).toOpenAPI(info).toYaml

  lazy val listBucketClasses: Endpoint[Unit, Unit, List[BucketClass], Any] =
    endpoint.get
      .in("db")
      .out(jsonBody[List[BucketClass]])
      .description("List all Bucket Classes configured in this service")

  object Errors {
    case object NotFound extends GetBucketResponse {
      implicit val dec: Decoder[NotFound.type] = Decoder.const(NotFound)
      implicit val enc: Encoder[NotFound.type] = _ =>
        Json.obj("error" -> Json.fromString("could not find bucket"))
    }
  }

  sealed trait GetBucketResponse
  object GetBucketResponse {
    val NotFound = Errors.NotFound
    case class Found(bucket: BucketClass) extends GetBucketResponse
    object Found {
      implicit val dec: Decoder[Found] = Decoder[BucketClass].map(Found(_))
      implicit val enc: Encoder[Found] = Encoder[BucketClass].contramap(_.bucket)
    }
  }

  lazy val getBucket: Endpoint[String, Unit, GetBucketResponse, Any] =
    endpoint.get
      .in("db")
      .in(path[String]("bucketClass"))
      .out(
        oneOf[GetBucketResponse](
          statusMapping(
            StatusCode.NotFound,
            jsonBody[Errors.NotFound.type]
              .description("not found")),
          statusMapping(
            StatusCode.Ok,
            jsonBody[GetBucketResponse.Found]
              .description("the bucket class queried"))
        )
      )
      .description("Get details on a specific Bucket Class")

  lazy val putIds: Endpoint[(String, String, List[String]), Errors.NotFound.type, Long, Any] =
    endpoint.post
      .in("db")
      .in(path[String]("bucketClass"))
      .in(path[String]("bucketName"))
      .in(jsonBody[List[String]])
      .out(jsonBody[Long])
      .errorOut(jsonBody[Errors.NotFound.type])
      .description("Add some IDs to a bucket and get the new estimated cardinality")

  lazy val getCardinality: Endpoint[(String, String), Errors.NotFound.type, Long, Any] =
    endpoint.get
      .in("db")
      .in(path[String]("bucketClass"))
      .in(path[String]("bucketName"))
      .out(jsonBody[Long])
      .errorOut(jsonBody[Errors.NotFound.type])
      .description("Get the estimated cardinality of a bucket")
}
