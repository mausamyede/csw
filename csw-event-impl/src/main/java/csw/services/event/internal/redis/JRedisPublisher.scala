package csw.services.event.internal.redis

import acyclic.skipped
import csw.services.event.javadsl.IEventPublisher

class JRedisPublisher(redisPublisher: RedisPublisher) extends IEventPublisher(redisPublisher)
