package ar.com.crowsoft.diffservice

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.handleRejections
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ar.com.crowsoft.diffservice.io.FileActor
import ar.com.crowsoft.diffservice.routes.{DiffServiceRejectionHandler, DiffServiceRoutes}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object DiffService extends App with DiffServiceRoutes with DiffServiceRejectionHandler {

  private lazy val defaultConfig     = ConfigFactory.parseResources("base.conf")
  private lazy val environmentConfig = ConfigFactory.parseResources("reference.conf")
  private lazy val userConfig        = ConfigFactory.parseResources("application.conf")

  val config = userConfig
    .withFallback(environmentConfig)
    .withFallback(defaultConfig)
    .resolve()

  implicit val system: ActorSystem = ActorSystem("diff-service-actor-system", config)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val fileActor = system.actorOf(FileActor.props(config), "fileActor")

  lazy val routes: Route = handleRejections(diffServiceRejectionHandler) {
    diffServiceRoutes
  }

  lazy val port = config.getInt("diff-service.port")

  Http().bindAndHandle(routes, "0.0.0.0", port)

  println(s"Server online at http://0.0.0.0:${port}")

  Await.result(system.whenTerminated, Duration.Inf)

}
