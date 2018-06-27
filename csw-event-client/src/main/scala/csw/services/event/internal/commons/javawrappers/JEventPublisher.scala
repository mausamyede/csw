package csw.services.event.internal.commons.javawrappers

import java.util.concurrent.CompletableFuture
import java.util.function.{Consumer, Supplier}

import akka.Done
import akka.actor.Cancellable
import akka.stream.javadsl.Source
import csw.messages.events.Event
import csw.services.event.exceptions.PublishFailure
import csw.services.event.javadsl.IEventPublisher
import csw.services.event.scaladsl.EventPublisher

import scala.compat.java8.FunctionConverters.{enrichAsScalaFromConsumer, enrichAsScalaFromSupplier}
import scala.compat.java8.FutureConverters.FutureOps
import scala.concurrent.duration.FiniteDuration

class JEventPublisher(eventPublisher: EventPublisher) extends IEventPublisher {
  override def publish(event: Event): CompletableFuture[Done] = eventPublisher.publish(event).toJava.toCompletableFuture

  override def publish[Mat](source: Source[Event, Mat]): Mat = eventPublisher.publish(source.asScala)

  override def publish[Mat](source: Source[Event, Mat], onError: Consumer[PublishFailure]): Any =
    eventPublisher.publish(source.asScala, onError.asScala)

  override def publish(eventGenerator: Supplier[Event], every: FiniteDuration): Cancellable =
    eventPublisher.publish(eventGenerator.asScala.apply(), every)

  override def publish(eventGenerator: Supplier[Event], every: FiniteDuration, onError: Consumer[PublishFailure]): Cancellable =
    eventPublisher.publish(eventGenerator.asScala.apply(), every, onError.asScala)

  override def shutdown(): CompletableFuture[Done] = eventPublisher.shutdown().toJava.toCompletableFuture

  def asScala: EventPublisher = eventPublisher
}