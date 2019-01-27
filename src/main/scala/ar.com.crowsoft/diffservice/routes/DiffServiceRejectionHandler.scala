package ar.com.crowsoft.diffservice.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import ar.com.crowsoft.diffservice.logging.Logging

trait DiffServiceRejectionHandler extends Logging {

  private def errorString(error: String, label: String = "error") = compact(render(label -> error))

  implicit class DiffServiceRoute(r: Route) {
    import akka.http.scaladsl.server.Directives._

    def withRejectionHandler(handler: RejectionHandler = diffServiceRejectionHandler): Route = {
      handleRejections(handler) { r }
    }
  }

  implicit def diffServiceRejectionHandler: RejectionHandler = RejectionHandler.newBuilder().handle {
    case MalformedQueryParamRejection(parameter, message, cause) =>
      complete {
        log.warning(s"Malformed content for parameter $parameter: $message ${cause.mkString}")
        val entityMessage = s"Rejected: $message"
        HttpResponse(status = StatusCodes.BadRequest, entity = errorString(entityMessage, parameter))
      }
    case MalformedRequestContentRejection(message, cause) =>
      complete {
        log.error(s"Malformed content $message", cause)
        HttpResponse(status = StatusCodes.BadRequest, entity = errorString(message))
      }

    case InvalidRequiredValueForQueryParamRejection(parameter, expected, actual) =>
      complete {
        log.warn(s"Invalid parameter value ($parameter): Expected $expected, Actual $actual")
        val userMessage = s"[$parameter]. Expected $expected, actual $actual"
        HttpResponse(StatusCodes.BadRequest, entity = errorString(userMessage))
      }
    case ValidationRejection(_, Some(FileIDNotFoundException)) =>
      complete {
        HttpResponse(StatusCodes.NotFound, entity = errorString("Requested Entity not found"))
      }

    case ValidationRejection(message, maybeCause) =>
      complete {
        maybeCause.foreach( t => log.error(t, s"Validation rejection: $message"))
        HttpResponse(StatusCodes.BadRequest, entity = errorString(message))
      }
  }.handleNotFound {
    complete (HttpResponse(StatusCodes.NotFound, entity = "[DiffService] The requested resource could not be found"))
  }.result()
}

object DiffServiceRejectionHandler extends DiffServiceRejectionHandler

