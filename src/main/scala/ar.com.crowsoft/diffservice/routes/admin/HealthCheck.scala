package ar.com.crowsoft.diffservice.routes.admin

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ar.com.crowsoft.diffservice.logging.Logging

object HealthCheck extends Logging {

  def apply()(implicit system: ActorSystem): Route = {

    pathPrefix("admin") {
      path("health-check") {
        get {
          complete {
            log.debug("health-check")
            "OK"
          }
        }
      }
    }
  }
}
