package ar.com.crowsoft.diffservice.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestActor.AutoPilot
import akka.testkit.{TestActor, TestProbe}
import ar.com.crowsoft.diffservice.actorSystemSupport.TestActorSystem
import ar.com.crowsoft.diffservice.io.DiffActor.{Compare, DiffResult}
import ar.com.crowsoft.diffservice.io.FileActor.{FileSaveResult, SaveFile}
import com.typesafe.config.ConfigFactory
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.compact
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._

class DiffServiceRoutesSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with DiffServiceRejectionHandler
  with BeforeAndAfterAll {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(30 second)

  lazy val diffServiceCfg = ConfigFactory.parseString {
    s"""
      diff-service {
        request-timeout-in-seconds = 15
      }
      """.stripMargin
  }

  lazy val akkaNode = ConfigFactory.parseString {
    """
      akka {
        loglevel: ERROR
        netty.tcp.port: 9132
        remote.netty.tcp.port: 9133
      }
      """.stripMargin
  }

  lazy val base = diffServiceCfg.withFallback(akkaNode)

  override def createActorSystem() = {

    TestActorSystem("DiffServiceRoutesSpecSystem", base, useFileFallback = false)
  }

  def testActorSystem = this.system

  val fileActorTest = TestProbe("fileActor")
  val diffActorTest = TestProbe("diffActor")

  class DiffServiceRouteTest extends DiffServiceRoutes {

    implicit val system = testActorSystem

    val fileActor = fileActorTest.ref

    val diffActor = diffActorTest.ref

    val config = base
  }

  def withRoute(f: Route => Any) : Any = {

    val route = new DiffServiceRouteTest().diffServiceRoutes.withRejectionHandler(diffServiceRejectionHandler)

    f(route)
  }

  class FileActorAutoPilot(implicit system: ActorSystem) extends AutoPilot {

    override def run(sender: ActorRef, msg: Any) = {
      msg match {
        case SaveFile(id, fileData, fileSide) =>
          sender ! FileSaveResult("some path", "file saved!")
          TestActor.KeepRunning

        case _ => TestActor.NoAutoPilot
      }
    }
  }

  fileActorTest.setAutoPilot(new FileActorAutoPilot())

  def testSaveFile(side: String)(route: Route) = {
    val data = "xxx"
    val name = "fileName"
    val body = compact(("name" -> name) ~~ ("data" -> data))
    val fileId = "id1"

    Put(s"/diffservice/v1/diff/$fileId/$side", body) ~> route ~> check {
      response.status should be (StatusCodes.Created)

      fileActorTest.expectMsgPF(){
        case SaveFile(id, fileData, fileSide) =>
          id should be(fileId)
          fileData.name should be(name)
          fileData.data should be(data)
      }
    }
  }

  class DiffActorAutoPilot(implicit system: ActorSystem) extends AutoPilot {

    override def run(sender: ActorRef, msg: Any) = {
      msg match {
        case Compare(id) if id == "id1" =>
          sender ! DiffResult(304, "The files are identical")
          TestActor.KeepRunning

        case Compare(id) if id == "id2" =>
          sender ! DiffResult(409, "The files's size aren't equal")
          TestActor.KeepRunning

        case Compare(id) if id == "id3" =>
          sender ! DiffResult(600, "Bothf files are missing")
          TestActor.KeepRunning

        case _ => TestActor.NoAutoPilot
      }
    }
  }

  diffActorTest.setAutoPilot(new DiffActorAutoPilot())

  "File Route" when {
    "payload is jObject" should {

      "reject the request if name is not present" in withRoute { route =>
        val body = compact("data" -> "xxx")

        Put("/diffservice/v1/diff/id1/left", body) ~> route ~> check {
          response.status should be (StatusCodes.BadRequest)
        }
      }

      "reject the request if data is not present" in withRoute { route =>
        val body = compact("name" -> "file")

        Put("/diffservice/v1/diff/id1/left", body) ~> route ~> check {
          response.status should be (StatusCodes.BadRequest)
        }
      }

      "Save left file if name and data are present" in withRoute(testSaveFile("left"))

      "Save rigth file if name and data are present" in withRoute(testSaveFile("right"))

    }
  }

  "Diff Route" when {
    "id is present in storage" should {
      "response with diff result" in withRoute { route =>

        Get("/diffservice/v1/diff/id1") ~> route ~> check {
          response.status should be (StatusCodes.OK)
        }

        Get("/diffservice/v1/diff/id2") ~> route ~> check {
          response.status should be (StatusCodes.OK)
        }

        Get("/diffservice/v1/diff/id3") ~> route ~> check {
          response.status should be (StatusCodes.NotFound)
        }

      }
    }
  }
}