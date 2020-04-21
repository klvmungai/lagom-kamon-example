package org.example.hello.impl

import akka.Done
import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.grpc.scaladsl.Metadata
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import kamon.Kamon

import scala.concurrent.duration._
import org.example.hello.grpc
import org.example.hello.grpc.{AbstractGreeterServicePowerApiRouter, HelloReply, HelloRequest, SetGreetingReply}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class HelloGrpcServiceImpl(sys: ActorSystem,
                           clusterSharding: ClusterSharding)
                          (implicit ec: ExecutionContext) extends AbstractGreeterServicePowerApiRouter(sys) {
  implicit val timeout: Timeout = Timeout(5.seconds)
  protected final val log: Logger = LoggerFactory.getLogger(getClass)

  override def sayHello(in: HelloRequest, metadata: Metadata): Future[HelloReply] = {
    val current = Kamon.currentSpan()
    log.info("Current GRPC span " + current.operationName())
    log.info("GRPC Sampling " + current.trace.samplingDecision)
    //current.takeSamplingDecision()
    log.info("Current GRPC context " + Kamon.currentContext())
    val span = Kamon.spanBuilder("hello-grpc-users")
      .tag("string-tag", "hello")
      .tag("boolean-tag", true)
      .start()
    Kamon.runWithSpan(span) {
      val ref = entityRef(in.id)
      ref.ask[Greeting](replyTo => Hello(in.id, replyTo))
        .map(greeting => HelloReply(greeting.message))
    }
  }

  override def setGreeting(in: grpc.Greeting, metadata: Metadata): Future[SetGreetingReply] = {
    val ref = entityRef(in.id)
    ref
      .ask[Confirmation](
        replyTo => UseGreetingMessage(in.message, replyTo)
      )
      .map {
        case Accepted => SetGreetingReply()
        case _        => throw BadRequest("Can't upgrade the greeting message.")
      }
  }

  /**
   * Looks up the entity for the given ID.
   */
  private def entityRef(id: String): EntityRef[HelloCommand] =
    clusterSharding.entityRefFor(HelloState.typeKey, id)
}
