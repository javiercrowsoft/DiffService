package ar.com.crowsoft.diffservice.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crowsoft.diffservice.io.DiffActor._
import ar.com.crowsoft.diffservice.io.FileActor._
import ar.com.crowsoft.diffservice.io.{FileData, FileSide, LeftFile, RightFile, isMissingCode}
import ar.com.crowsoft.diffservice.logging.Logging
import ar.com.crowsoft.diffservice.routes.admin.HealthCheck
import com.typesafe.config.Config
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait DiffServiceRoutes extends Logging {

  implicit def system: ActorSystem

  def config: Config

  def fileActor: ActorRef

  def diffActor: ActorRef

  private implicit lazy val timeout = Timeout(config.getInt("diff-service.request-timeout-in-seconds").seconds)

  implicit val formats = DefaultFormats

  lazy val diffServiceRoutes: Route =
    pathPrefix("diffservice" / "v1" / "diff") {
      saveFileRoute(LeftFile()) ~ saveFileRoute(RightFile()) ~ diffRoute ~ HealthCheck()
    }

  def saveFileRoute(fileSide: FileSide) = path(Segment / fileSide.name.toLowerCase) { (id) =>
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
          case Success(saveResult) =>
            log.info(s"$action: {}", saveResult.path)
            (code, saveResult.description)
          case Failure(e) =>
            log.error(e, s"Error when $action")
            HttpResponse(500, entity = "Operation has failed")
        }
      }
    }
  }

  def diffRoute = path(Segment) { (id) =>
    val result = (diffActor ? Compare(id)).mapTo[DiffResult]
    onComplete(result) { resultTry =>
      complete {
        resultTry match {
          case Success(diffResult) =>
            log.info(s"diff result: {}", diffResult.description)
            val statusCode = diffResult.result match {
              case code if isMissingCode(code) => StatusCodes.NotFound
              case _ => StatusCodes.OK
            }
            (statusCode, write(diffResult))
          case Failure(e) =>
            log.error(e, s"Error when comparing files")
            HttpResponse(500, entity = "Operation has failed")
        }
      }
    }
  }
}
