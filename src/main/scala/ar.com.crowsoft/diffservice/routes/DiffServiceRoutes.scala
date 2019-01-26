package ar.com.crowsoft.diffservice.routes

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import ar.com.crowsoft.diffservice.routes.admin.HealthCheck

trait DiffServiceRoutes {

  implicit def system: ActorSystem

  lazy val diffServiceRoutes: Route =
    pathPrefix("diffservice") {
      HealthCheck()
    }
}
