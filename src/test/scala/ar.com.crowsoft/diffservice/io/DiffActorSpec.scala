package ar.com.crowsoft.diffservice.io

import java.nio.file.{Path, Paths}

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crowsoft.diffservice.actorSystemSupport.TestActorSystem
import ar.com.crowsoft.diffservice.io.DiffActor.{Compare, DiffResult}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class DiffActorSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll {

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
        netty.tcp.port: 9136
        remote.netty.tcp.port: 9137
      }
      """.stripMargin
  }

  lazy val base = diffServiceCfg.withFallback(akkaNode)

  override def createActorSystem() = {

    TestActorSystem("DiffActorSpecSystem", base)
  }

  object FileTest extends File {
    def saveFile(folder: String, filename: String, bytes: Array[Byte]): Path = {
      Paths.get(s"$folder/$filename").toAbsolutePath
    }
    def diffFiles(filenameLeft: String, filenameRight: String): CompareInfo = filenameLeft match {
      case fn if fn.contains("id1")=> CompareInfo(Identical)
      case fn if fn.contains("id2")=> CompareInfo(SizeNotEqual)
      case fn if fn.contains("id3")=> CompareInfo(LeftMissing)
    }
  }

  val diffActor = system.actorOf(DiffActor.props(base, FileTest), "diffActor")

  private implicit lazy val timeout = Timeout(2.seconds)

  def compareFile(fileId: String, code: Int) = {

    val result = (diffActor ? Compare(fileId)).mapTo[DiffResult]

    result onComplete {
      case Success(diffResult) =>
        diffResult.result should be(code)
      case Failure(e) =>
        fail()
    }
  }

  "DiffActor" when {
    "Compare message is sent" should {

      "When file are identical it returns 304" in compareFile("id1", 304)

      "When file are identical it returns 409" in compareFile("id2", 409)

      "When left file is missing it returns 601" in compareFile("id3", 601)

    }
  }
}