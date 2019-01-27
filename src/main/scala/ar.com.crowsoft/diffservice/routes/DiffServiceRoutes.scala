package ar.com.crowsoft.diffservice.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crowsoft.diffservice.io.FileActor._
import ar.com.crowsoft.diffservice.io.{FileData, FileSide, LeftFile, RightFile}
import ar.com.crowsoft.diffservice.logging.Logging
import ar.com.crowsoft.diffservice.routes.admin.HealthCheck
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait DiffServiceRoutes extends Logging {

  implicit def system: ActorSystem

  def config: Config

  def fileActor: ActorRef

  private implicit lazy val timeout = Timeout(config.getInt("diff-service.request-timeout-in-seconds").seconds)

  lazy val diffServiceRoutes: Route =
    pathPrefix("diffservice") {
      saveFileRoute(LeftFile()) ~ saveFileRoute(RightFile()) ~ HealthCheck()
    }

  def saveFileRoute(fileSide: FileSide) = path("v1" / "diff" / Segment / fileSide.name.toLowerCase) { (id) =>
    put {
      entity(as[FileData]) { fileData => saveFile(id, fileData, fileSide) }
    }

  }

  def saveFile(id: String, fileData: FileData, fileSide: FileSide) = {
    val result = (fileActor ? SaveFile(id, fileData, fileSide)).mapTo[FileSaveResult]
    completeAction(result, s"save ${fileSide.name} file ${fileData.name} for id $id", StatusCodes.Created)
  }

  def completeAction(result: Future[FileSaveResult], action: String, code: StatusCode) = {
    onComplete(result) { resultTry =>
      complete {
        resultTry match {
          case Success(a) =>
            log.info(s"$action: {}", a.path)
            (code, a.description)
          case Failure(e) =>
            log.error(e, s"Error when $action")
            HttpResponse(500, entity = "Operation has failed")
        }
      }
    }
  }
}
