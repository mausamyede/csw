package csw.services.event.internal.pubsub

import akka.Done
import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import csw.messages.events.Event
import csw.services.event.exceptions.PublishFailedException
import csw.services.event.internal.commons.EventServiceLogger

import scala.concurrent.duration.{DurationDouble, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class EventPublisherUtil(implicit ec: ExecutionContext, mat: Materializer) {

  private val logger = EventServiceLogger.getLogger

  def eventSource(eventGenerator: => Event, every: FiniteDuration): Source[Event, Cancellable] =
    Source.tick(0.millis, every, ()).map(_ => withErrorLogging(eventGenerator))

  def publishFromSource[Mat](
      source: Source[Event, Mat],
      parallelism: Int,
      publish: Event ⇒ Future[Done],
      maybeOnError: Option[Event ⇒ Unit]
  ): Mat =
    source
      .mapAsync(parallelism) { event ⇒
        publishWithRecovery(event, publish, maybeOnError)
      }
      .to(Sink.ignore)
      .run()

  private def publishWithRecovery(event: Event, publish: Event ⇒ Future[Done], maybeOnError: Option[Event ⇒ Unit]) =
    publish(event).recover[Done] {
      case _ @PublishFailedException(e) ⇒
        maybeOnError.foreach(onError ⇒ onError(e))
        Done
    }

  private def withErrorLogging(eventGenerator: => Event): Event =
    try {
      eventGenerator
    } catch {
      case NonFatal(ex) ⇒
        logger.error(ex.getMessage, ex = ex)
        throw ex
    }

}