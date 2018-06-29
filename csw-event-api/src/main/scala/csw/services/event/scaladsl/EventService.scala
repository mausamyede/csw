package csw.services.event.scaladsl

import scala.concurrent.{ExecutionContext, Future}

/**
 * An interface to provide access to [[csw.services.event.scaladsl.EventPublisher]] and [[csw.services.event.scaladsl.EventSubscriber]].
 */
trait EventService {
  implicit val executionContext: ExecutionContext

  /**
   * A default instance of [[csw.services.event.scaladsl.EventPublisher]].
   * This could be shared across under normal operating conditions to share the underlying connection to event server.
   */
  lazy val defaultPublisher: Future[EventPublisher] = makeNewPublisher()

  /**
   * A default instance of [[csw.services.event.scaladsl.EventSubscriber]].
   * This could be shared across under normal operating conditions to share the underlying connection to event server.
   */
  lazy val defaultSubscriber: Future[EventSubscriber] = makeNewSubscriber()

  /**
   * Create a new instance of [[csw.services.event.scaladsl.EventPublisher]] with a separate underlying connection than the default instance.
   * The new instance will be required when the location of Event Service is updated or in case the performance requirements
   * of a publish operation demands a separate connection to be used.
   * @return
   */
  def makeNewPublisher(): Future[EventPublisher]

  /**
   * Create a new instance of [[csw.services.event.scaladsl.EventPublisher]] with a separate underlying connection than the default instance.
   * The new instance will be required when the location of Event Service is updated or in case the performance requirements
   * of a subscribe operation demands a separate connection to be used.
   * @return A new instance of [[csw.services.event.scaladsl.EventSubscriber]]
   */
  def makeNewSubscriber(): Future[EventSubscriber]
}
