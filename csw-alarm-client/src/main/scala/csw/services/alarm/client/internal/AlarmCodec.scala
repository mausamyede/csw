package csw.services.alarm.client.internal

import java.nio.ByteBuffer

import csw.services.alarm.api.internal._
import csw.services.alarm.api.models._
import csw.services.alarm.client.internal.redis.scala_wrapper.RedisKeySpaceCodec
import io.lettuce.core.codec.Utf8StringCodec
import ujson.Js
import upickle.default._

import scala.util.control.NonFatal

class AlarmCodec[K: ReadWriter, V: ReadWriter] extends RedisKeySpaceCodec[K, V] {
  private lazy val utf8StringCodec = new Utf8StringCodec()

  override def encodeKey(key: K): ByteBuffer     = utf8StringCodec.encodeKey(writeJs(key).str)
  override def decodeKey(byteBuf: ByteBuffer): K = read[K](Js.Str(utf8StringCodec.decodeKey(byteBuf)))

  override def encodeValue(value: V): ByteBuffer   = utf8StringCodec.encodeValue(write(value))
  override def decodeValue(byteBuf: ByteBuffer): V = read[V](utf8StringCodec.decodeValue(byteBuf))

  override def toKeyString(key: K): String         = utf8StringCodec.decodeKey(encodeKey(key))
  override def fromKeyString(keyString: String): K = decodeKey(utf8StringCodec.encodeKey(keyString))
}

object AlarmCodec extends AlarmRW {
  implicit object MetadataCodec extends AlarmCodec[MetadataKey, AlarmMetadata]
  implicit object StatusCodec   extends AlarmCodec[StatusKey, AlarmStatus]
  implicit object SeverityCodec extends AlarmCodec[SeverityKey, AlarmSeverity] {
    override def decodeValue(byteBuf: ByteBuffer): AlarmSeverity = {
      //TODO: get rid of try-catch? what about events?
      try {
        super.decodeValue(byteBuf)
      } catch {
        case NonFatal(_) ⇒ AlarmSeverity.Disconnected // if severity expires than cast the null returned value to Disconnected
      }
    }
  }
}
