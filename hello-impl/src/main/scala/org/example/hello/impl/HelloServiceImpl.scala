package org.example.hello.impl

import org.example.hello.api
import org.example.hello.api.HelloService
import akka.Done
import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import kamon.Kamon
import org.slf4j.{Logger, LoggerFactory}

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
  extends HelloService {

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[HelloCommand] =
    clusterSharding.entityRefFor(HelloState.typeKey, id)

  implicit val timeout: Timeout = Timeout(5.seconds)
  protected final val log: Logger = LoggerFactory.getLogger(getClass)

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall {
    _ =>
      val current = Kamon.currentSpan()
      log.info("Current HTTP span " + current.operationName())
      log.info("HTTP Sampling " + current.trace.samplingDecision)
      //current.takeSamplingDecision()
      log.info("Current HTTP context " + Kamon.currentContext())
      val span = Kamon.spanBuilder("hello-http-users")
        .start()
      Kamon.runWithSpan(span) {
        // Look up the sharded entity (aka the aggregate instance) for the given ID.
        val ref = entityRef(id)

        // Ask the aggregate instance the Hello command.
        ref
          .ask[Greeting](replyTo => Hello(id, replyTo))
          .map(greeting => greeting.message)
      }
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the sharded entity (aka the aggregate instance) for the given ID.
    val ref = entityRef(id)

    // Tell the aggregate to use the greeting message specified.
    ref
      .ask[Confirmation](
        replyTo => UseGreetingMessage(request.message, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't upgrade the greeting message.")
      }
  }

//  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
//    TopicProducer.singleStreamWithOffset { fromOffset =>
//      persistentEntityRegistry
//        .eventStream(HelloEvent.Tag, fromOffset)
//        .map(ev => (convertEvent(ev), ev.offset))
//    }
//
//  private def convertEvent(
//    helloEvent: EventStreamElement[HelloEvent]
//  ): api.GreetingMessageChanged = {
//    helloEvent.event match {
//      case GreetingMessageChanged(msg) =>
//        api.GreetingMessageChanged(helloEvent.entityId, msg)
//    }
//  }
}
