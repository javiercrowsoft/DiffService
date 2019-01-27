package ar.com.crowsoft.diffservice.io

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config
import java.util.Base64

object FileActor {
  def props(config: Config, file: File) = Props(classOf[FileActor], config, file)

  case class SaveFile(id: String, fileData: FileData, fileSide: FileSide)
  case class FileSaveResult(path: String, description: String)
}

class FileActor(config: Config, file: File) extends Actor with ActorLogging {

  import FileActor._

  lazy val storagePath = config.getString("diff-service.storage-path")

  def receive: Receive = {
      case SaveFile(id, fileData, fileSide) =>
        val bytes = Base64.getDecoder().decode(fileData.data)
        val folder = s"$storagePath/$id"
        val filename = s"${fileSide.name}.out"
        val path = file.saveFile(folder, filename, id, bytes)
        sender() ! FileSaveResult(path.toAbsolutePath.toString, s"file saved!")
    }


}
