package ar.com.crowsoft.diffservice.io

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config
import java.util.Base64

object DiffActor {
  def props(config: Config, file: File) = Props(classOf[DiffActor], config, file)

  case class Compare(id: String)
  case class DiffResult(description: String)
}

class DiffActor(config: Config, file: File) extends Actor with ActorLogging {

  import DiffActor._

  lazy val storagePath = config.getString("diff-service.storage-path")

  def receive: Receive = {
    case Compare(id) =>
      sender() ! DiffResult(s"I don't know :P !")
  }


}
