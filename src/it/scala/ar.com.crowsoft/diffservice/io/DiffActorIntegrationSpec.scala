package ar.com.crowsoft.diffservice.io

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.ask
import akka.util.Timeout
import ar.com.crowsoft.diffservice.io.DiffActor.{Compare, DiffResult}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class DiffActorIntegrationSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll {

  private lazy val defaultConfig     = ConfigFactory.parseResources("base.conf")
  private lazy val environmentConfig = ConfigFactory.parseResources("reference.conf")
  private lazy val userConfig        = ConfigFactory.parseResources("application.conf")

  val config = userConfig
    .withFallback(environmentConfig)
    .withFallback(defaultConfig)
    .resolve()

  override def createActorSystem() = {

    ActorSystem("DiffActorIntegrationSpecSystem", config)
  }

  val diffActor = system.actorOf(DiffActor.props(config, File), "diffActor")

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

      "When file are same size but not equal it returns 304" in compareFile("id1", 304)

      "When file are same size but not equal it returns 409" in compareFile("id2", 409)

      "When files's size is not equal it returns 409" in compareFile("id3", 409)

      "When files are missing it returns 404" in compareFile("id4", 404)

    }
  }
}