package ar.com.crowsoft.diffservice.io

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.typesafe.config.Config

import scala.concurrent.Future

object DiffActor {
  def props(config: Config, file: File) = Props(classOf[DiffActor], config, file)

  case class Compare(id: String)
  case class DiffDetail(offset: Int, length: Int)
  case class DiffResult(result: Int, description: String, diffs: List[DiffDetail] = List())
}

class DiffActor(config: Config, file: File) extends Actor with ActorLogging {

  import DiffActor._

  implicit val blockingDispatcher = context.system.dispatchers.lookup("akka.io-blocking-dispatcher")

  lazy val storagePath = config.getString("diff-service.storage-path")

  def receive: Receive = {
    case Compare(id) =>
      val f = Future {
        val folder = s"$storagePath/$id"
        val compareInfo = file.diffFiles(s"$folder/${LeftFile()}.out", s"$folder/${RightFile()}.out")
        DiffResult(compareInfo.result.code, compareInfo.result.description, compareInfo.diffs)
      }

      f pipeTo sender()
  }


}
