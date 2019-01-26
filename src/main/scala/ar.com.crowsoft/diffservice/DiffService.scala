package ar.com.crowsoft.diffservice

import akka.actor._
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.{AskTimeoutException, Patterns}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Try

import ar.com.crowsoft.diffservice.routes.DiffServiceRoutes

object DiffService extends App with DiffServiceRoutes {

  private lazy val defaultConfig     = ConfigFactory.parseResources("base.conf")
  private lazy val environmentConfig = ConfigFactory.parseResources("reference.conf")
  private lazy val userConfig        = ConfigFactory.parseResources("application.conf")

  private lazy val config = userConfig
    .withFallback(environmentConfig)
    .withFallback(defaultConfig)
    .resolve()

  implicit val system: ActorSystem = ActorSystem("diff-service-actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val routes: Route = diffServiceRoutes
  lazy val port = config.getInt("diff-service.port")

  Http().bindAndHandle(routes, "0.0.0.0", port)

  println(s"Server online at http://0.0.0.0:${port}")

  Await.result(system.whenTerminated, Duration.Inf)

}
