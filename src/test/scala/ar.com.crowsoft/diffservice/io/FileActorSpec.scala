package ar.com.crowsoft.diffservice.io

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crowsoft.diffservice.actorSystemSupport.TestActorSystem
import ar.com.crowsoft.diffservice.io.FileActor.{FileSaveResult, SaveFile}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FileActorSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(30 second)

  lazy val diffServiceCfg = ConfigFactory.parseString {
    s"""
      diff-service {
        storage-path = "storage"
      }
      """.stripMargin
  }

  lazy val akkaNode = ConfigFactory.parseString {
    """
      akka {
        loglevel: ERROR
        netty.tcp.port : 9132
        remote.netty.tcp.port: 9133
      }
      """.stripMargin
  }

  lazy val base = diffServiceCfg.withFallback(akkaNode)

  override def createActorSystem() = {

    TestActorSystem("FileActorSpecSystem", base)
  }

  object FileTest extends File {
    def saveFile(folder: String, filename: String, id: String, bytes: Array[Byte]): Path = {
      Paths.get(s"$folder/$filename").toAbsolutePath
    }
  }

  val fileActor = system.actorOf(FileActor.props(base, FileTest), "fileActor")

  private implicit lazy val timeout = Timeout(2.seconds)

  def testSaveFile(fileSide: FileSide) = {
    val data = "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"
    val name = "fileName"
    val fileId = "id1"
    val folder = base.getString("diff-service.storage-path")
    val filename = s"${fileSide.name}.out"
    val absolutePath = Paths.get(s"$folder/$filename").toAbsolutePath.toString

    val result = (fileActor ? SaveFile(fileId, FileData(name, data), fileSide)).mapTo[FileSaveResult]

        result onComplete {
          case Success(a) =>
            a.path should be(absolutePath)
          case Failure(e) =>
            fail()
        }
  }

  "FileActor" when {
    "SaveFile message is sent" should {

      "Save the file left.out in storage folder" in testSaveFile(LeftFile())

      "Save the file right.out in storage folder" in testSaveFile(RightFile())

    }
  }
}