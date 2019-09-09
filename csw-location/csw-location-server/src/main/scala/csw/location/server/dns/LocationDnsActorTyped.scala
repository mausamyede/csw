package csw.location.server.dns

import akka.actor.Scheduler
import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.{TypedActorRefOps, TypedActorSystemOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SpawnProtocol}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.github.mkroli.dns4s.Message
import com.github.mkroli.dns4s.akka.Dns
import com.github.mkroli.dns4s.dsl._
import csw.location.api.scaladsl.LocationService
import csw.location.models.Connection.HttpConnection
import csw.location.models.{ComponentId, ComponentType}
import csw.location.server.dns.LocationDnsActorTyped.{DnsActorMessage, DnsResolution, StandardMessage}

import scala.async.Async.{async, await}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class LocationDnsActorTyped(locationService: LocationService) {
  def defaultBehaviour: Behavior[DnsActorMessage] =
    Behaviors.setup[DnsActorMessage] { ctx =>
      import ctx.executionContext
      Behaviors.receiveMessage[DnsActorMessage] {
        case s @ StandardMessage(Query(_) ~ Questions(QName(host) ~ TypeA() :: Nil), _) =>
          ctx.pipeToSelf(resolve(host))(DnsResolution(_, s))
          Behaviors.same

        case DnsResolution(Success(ip), StandardMessage(Query(q) ~ Questions(QName(host) ~ TypeA() :: Nil), sender)) =>
          sender ! Response(q) ~ Answers(RRName(host) ~ ARecord(ip)) ~ AuthoritativeAnswer
          Behaviors.same

        case DnsResolution(Failure(_), originalMessage) =>
          originalMessage.sender ! failedResponse(originalMessage.message)
          Behaviors.same

        case StandardMessage(message, sender) =>
          sender ! failedResponse(message)
          Behaviors.same
      }
    }

  private def failedResponse(message: Message): ComposableMessage = Response(message) ~ Refused

  private def resolve(host: String)(implicit executionContext: ExecutionContext): Future[String] = async {
    //todo: check component-id
    await(locationService.find(HttpConnection(ComponentId(s"$host-http", ComponentType.Service))))
      .map(_.uri.getHost)
      .getOrElse(throw new RuntimeException(s"could not resolve $host"))
  }
}

object LocationDnsActorTyped {

  sealed trait DnsActorMessage
  case class StandardMessage(message: Message, sender: ActorRef[Message])     extends DnsActorMessage
  case class DnsResolution(resolution: Try[String], message: StandardMessage) extends DnsActorMessage

  def start(port: Int, locationService: LocationService)(
      implicit actorSystem: ActorSystem[SpawnProtocol],
      timeout: Timeout
  ): Future[Any] = {
    implicit val sch: Scheduler     = actorSystem.scheduler
    implicit val untypedActorSystem = actorSystem.toUntyped
    import actorSystem.executionContext
    val behaviour = new LocationDnsActorTyped(locationService).defaultBehaviour

    for {
      actorRef   <- actorSystem ? Spawn(behaviour, "DnsActor")
      bindResult <- IO(Dns) ? Dns.Bind(actorRef.toUntyped, port)
    } yield bindResult
  }
}
