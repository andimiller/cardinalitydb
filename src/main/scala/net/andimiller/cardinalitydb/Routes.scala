package net.andimiller.cardinalitydb

import cats.data.{EitherT, OptionT}
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.transactions.RedisTransaction
import io.chrisdavenport.log4cats.Logger
import net.andimiller.cardinalitydb.models.BucketClass
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.tapir.server.http4s._
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import org.http4s.syntax.kleisli._

import scala.concurrent.ExecutionContext

class Routes[F[_]: Sync: ConcurrentEffect: ContextShift: Timer: Logger](
    classes: List[BucketClass],
    redis: RedisCommands[F, String, String]) {
  object dsl extends Http4sDsl[F]
  import dsl._

  val listBucketClasses: HttpRoutes[F] = API.listBucketClasses.toRoutes { _ =>
    classes.pure[F].map(_.asRight)
  }

  val getBucket: HttpRoutes[F] = API.getBucket.toRoutes { bucket =>
    classes
      .find(_.name == bucket)
      .toRight(API.NotFound)
      .pure[F]
  }

  val putIds: HttpRoutes[F] = API.putIds.toRoutes {
    case (bucket, identifier, ids) =>
      val key = s"${bucket}_${identifier}"
      EitherT
        .fromEither[F](
          classes
            .find(_.name == bucket)
            .toRight(API.NotFound)
        )
        .semiflatMap { bucketClass =>
          for {
            _     <- redis.pfAdd(key, ids: _*)
            count <- redis.pfCount(key)
            _ <- bucketClass.expiry.traverse { expiry =>
                  redis.ttl(key).map(_.isEmpty).ifM(redis.expire(key, expiry).void, ().pure[F])
                }
          } yield count
        }
        .value
  }

  val getCardinality = API.getCardinality.toRoutes {
    case (bucket, identifier) =>
      val key = s"${bucket}_${identifier}"
      EitherT
        .fromEither[F](
          classes
            .find(_.name == bucket)
            .toRight(API.NotFound)
        )
        .semiflatMap { _ => redis.pfCount(key) }
        .value
  }

  val clearCardinality = API.clearCardinality.toRoutes {
    case (bucket, identifier) =>
      val key = s"${bucket}_${identifier}"
      EitherT
        .fromEither[F](
          classes
            .find(_.name == bucket)
            .toRight(API.NotFound)
        )
        .semiflatMap { _ => redis.del(key) }
        .as(API.Message("Bucket cleared"))
        .value
  }

  val endpoints = listBucketClasses <+> getBucket <+> putIds <+> getCardinality <+> clearCardinality

  def serve(ec: ExecutionContext, port: Int) =
    BlazeServerBuilder[F](ec)
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(
        Router("/" -> (endpoints <+> new SwaggerHttp4s(API.openApiYaml).routes[F])).orNotFound
      )

}
