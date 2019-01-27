package ar.com.crowsoft.diffservice

package object io {

  trait FileSide {
    def name: String
  }

  case class LeftFile() extends FileSide {
    val name = "LEFT"
  }

  case class RightFile() extends FileSide {
    val name = "RIGHT"
  }
}
