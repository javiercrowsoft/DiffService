package ar.com.crowsoft.diffservice.io

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config

object DiffActor {
  def props(config: Config, file: File) = Props(classOf[DiffActor], config, file)

  case class Compare(id: String)
  case class DiffDetail(offset: Int, length: Int)
  case class DiffResult(result: Int, description: String, diffs: List[DiffDetail] = List())
}

class DiffActor(config: Config, file: File) extends Actor with ActorLogging {

  import DiffActor._

  lazy val storagePath = config.getString("diff-service.storage-path")

  def receive: Receive = {
    case Compare(id) =>
      val folder = s"$storagePath/$id"
      val compareInfo = file.diffFiles(s"$folder/${LeftFile()}.out", s"$folder/${RightFile()}.out")
      sender() ! DiffResult(compareInfo.result.code, compareInfo.result.description, compareInfo.diffs)
  }


}
