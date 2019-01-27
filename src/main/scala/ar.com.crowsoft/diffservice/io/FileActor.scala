package ar.com.crowsoft.diffservice.io

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config

object FileActor {
  def props(config: Config) = Props(classOf[FileActor], config)

  case class SaveFile(id: String, fileData: FileData, fileSide: FileSide)
  case class FileSaveResult(path: String, description: String)
}

class FileActor(config: Config) extends Actor with ActorLogging {

  import FileActor._

  def receive: Receive = {
      case SaveFile(id, fileData, fileSide) =>
        sender() ! FileSaveResult("some path", "file saved!")
    }
}
