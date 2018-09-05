package romaine.keyspace

import akka.stream.scaladsl.Source
import reactor.core.publisher.FluxSink.OverflowStrategy
import romaine.RedisResult
import romaine.async.RedisAsyncApi
import romaine.codec.RomaineStringCodec
import romaine.extensions.SourceExtensions.RichSource
import romaine.reactive.{RedisSubscription, RedisSubscriptionApi}

import scala.concurrent.{ExecutionContext, Future}

class RedisKeySpaceApi[K: RomaineStringCodec, V: RomaineStringCodec](
    redisSubscriptionApi: RedisSubscriptionApi[KeyspaceKey, String],
    redisAsyncApi: RedisAsyncApi[K, V],
    keyspacePrefix: KeyspaceId = KeyspaceId._0
)(implicit ec: ExecutionContext) {

  private val SetOperation     = "set"
  private val ExpiredOperation = "expired"

  def watchKeyspaceValue(
      keys: List[String],
      overflowStrategy: OverflowStrategy
  ): Source[RedisResult[K, Option[V]], RedisSubscription] =
    redisSubscriptionApi
      .psubscribe(keys.map(x => KeyspaceKey(keyspacePrefix, x)), overflowStrategy)
      .filter(pm => pm.value == SetOperation || pm.value == ExpiredOperation)
      .mapAsync(1) { pm =>
        val key = RomaineStringCodec[K].fromString(pm.key.value)
        pm.value match {
          case SetOperation     => redisAsyncApi.get(key).map(valueOpt ⇒ (key, valueOpt))
          case ExpiredOperation => Future((key, None))
        }
      }
      .collect {
        case (k, v) ⇒ RedisResult(k, v)
      }
      .distinctUntilChanged

}
//todo: support for delete and expired, etc
//todo: RedisWatchSubscription try to remove type parameter
