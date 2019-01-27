package ar.com.crowsoft.diffservice

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import ar.com.crowsoft.diffservice.io.FileData
import org.json4s.JsonAST.JValue
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

package object routes {

  case object FileIDNotFoundException extends Throwable

  implicit class JsonOps(value : JValue) {

    import org.json4s.{DefaultFormats, _}

    def maybeGet[A](field: String):Option[A] = {
      implicit val formats = DefaultFormats
      (value \ field) match {
        case JNothing | JNull => None
        case x: JString => Some(x.extract[String].asInstanceOf[A])
        case JInt(i) => Some(i.toInt.asInstanceOf[A])
        case JBool(b) => Some(b.asInstanceOf[A])
        case something =>
          Some(something.values.asInstanceOf[A])
      }
    }

  }

  val entity2FileData: Unmarshaller[HttpEntity, FileData] = {
      Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
        parse(data.decodeString(charset.value)) match {
          case json@JObject(_) =>
            val f = for {
              name <- json.maybeGet[String]("name")
              data <- json.maybeGet[String]("data")
            } yield FileData(name, data)
            f.get
          case other: JValue =>
            lazy val r = pretty(other)
            throw new IllegalArgumentException(s"Unable to retrieve JSON Object from incoming entity: $r") with NoStackTrace
        }
      }
    }

  implicit val request2FileData: FromRequestUnmarshaller[FileData] = FileDataUnmarshaller

  object FileDataUnmarshaller extends Unmarshaller[HttpRequest, FileData] {

    override def apply(request: HttpRequest)(implicit ec: ExecutionContext, materializer: Materializer): Future[FileData] = {
      entity2FileData(request.entity)
    }
  }

}
