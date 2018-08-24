package romaine.async

import io.lettuce.core.KeyValue
import io.lettuce.core.api.async.RedisAsyncCommands
import romaine.extensions.FutureExtensions.RichFuture

import scala.collection.JavaConverters.{iterableAsScalaIterableConverter, mapAsJavaMapConverter}
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}

class RedisAsyncApi[K, V](redisAsyncCommands: Future[RedisAsyncCommands[K, V]])(implicit ec: ExecutionContext) {
  def set(key: K, value: V): Future[Unit] =
    redisAsyncCommands.flatMap(_.set(key, value).toScala.failWith(s"Redis 'SET' operation failed for [key:$key value:$value]"))

  def mset(map: Map[K, V]): Future[Unit] =
    redisAsyncCommands.flatMap(_.mset(map.asJava).toScala.failWith(s"Redis 'MSET' operation failed for [map: $map]"))

  def setex(key: K, seconds: Long, value: V): Future[Unit] =
    redisAsyncCommands.flatMap(
      _.setex(key, seconds, value).toScala.failWith(s"Redis 'SETEX' operation failed for [key: $key, value: $value]")
    )

  def get(key: K): Future[Option[V]] = redisAsyncCommands.flatMap(_.get(key).toScala.map(Option(_)))

  def mget(keys: List[K]): Future[List[KeyValue[K, V]]] =
    redisAsyncCommands.flatMap(_.mget(keys: _*).toScala.map(_.asScala.toList))

  def keys(key: K): Future[List[K]] = redisAsyncCommands.flatMap(_.keys(key).toScala.map(_.asScala.toList))

  def exists(keys: K*): Future[Boolean] = redisAsyncCommands.flatMap(_.exists(keys: _*).toScala.map(_ == keys.size))

  def del(keys: List[K]): Future[Long] = redisAsyncCommands.flatMap(_.del(keys: _*).toScala.map(_.toLong))

  def pdel(pattern: K): Future[Long] =
    keys(pattern).flatMap(matchedKeys ⇒ if (matchedKeys.nonEmpty) del(matchedKeys) else Future.successful(0))

  def publish(key: K, value: V): Future[Long] = redisAsyncCommands.flatMap(_.publish(key, value).toScala.map(_.toLong))

  def quit(): Future[String] = redisAsyncCommands.flatMap(_.quit().toScala)
}