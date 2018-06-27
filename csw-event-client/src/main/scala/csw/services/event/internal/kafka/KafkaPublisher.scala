package csw.services.event.internal.kafka

import akka.Done
import akka.actor.Cancellable
import akka.kafka.ProducerSettings
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.messages.events.Event
import csw.services.event.exceptions.PublishFailure
import csw.services.event.internal.commons.EventPublisherUtil
import csw.services.event.scaladsl.EventPublisher
import org.apache.kafka.clients.producer.{Callback, ProducerRecord}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

class KafkaPublisher(producerSettings: ProducerSettings[String, Array[Byte]])(
    implicit ec: ExecutionContext,
    mat: Materializer
) extends EventPublisher {

  private val parallelism        = 1
  private val kafkaProducer      = producerSettings.createKafkaProducer()
  private val eventPublisherUtil = new EventPublisherUtil()

  override def publish(event: Event): Future[Done] = {
    val promisedDone: Promise[Done] = Promise()
    try {
      kafkaProducer.send(eventToProducerRecord(event), completePromise(event, promisedDone))
    } catch {
      case NonFatal(ex) ⇒ promisedDone.failure(PublishFailure(event, ex))
    }
    promisedDone.future
  }

  override def publish[Mat](source: Source[Event, Mat]): Mat =
    eventPublisherUtil.publishFromSource(source, parallelism, publish, None)

  override def publish[Mat](stream: Source[Event, Mat], onError: PublishFailure ⇒ Unit): Mat =
    eventPublisherUtil.publishFromSource(stream, parallelism, publish, Some(onError))

  override def publish(eventGenerator: ⇒ Event, every: FiniteDuration): Cancellable =
    publish(eventPublisherUtil.eventSource(eventGenerator, every))

  override def publish(eventGenerator: ⇒ Event, every: FiniteDuration, onError: PublishFailure ⇒ Unit): Cancellable =
    publish(eventPublisherUtil.eventSource(eventGenerator, every), onError)

  override def shutdown(): Future[Done] = Future {
    scala.concurrent.blocking(kafkaProducer.close())
    Done
  }

  private def eventToProducerRecord(event: Event): ProducerRecord[String, Array[Byte]] =
    new ProducerRecord(event.eventKey.key, Event.typeMapper.toBase(event).toByteArray)

  private def completePromise(event: Event, promisedDone: Promise[Done]): Callback = {
    case (_, null)          ⇒ promisedDone.success(Done)
    case (_, ex: Exception) ⇒ promisedDone.failure(PublishFailure(event, ex))
  }
}