package ar.com.crowsoft.diffservice.logging

trait Logging {
  val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

  implicit class LoggerAdapter(l: org.slf4j.Logger) {
    def error(t: Throwable, msg: String) = l.error(msg, t)

    def warning(msg: String) = l warn msg

    def warning(format: String, arg: Any*) = l warn(format, arg)
  }
}
