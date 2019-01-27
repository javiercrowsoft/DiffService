package ar.com.crowsoft.diffservice

import java.nio.file.{Files, Path, Paths}

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

  trait File {
    def saveFile(folder:String, filename: String, id: String, bytes: Array[Byte]): Path
  }

  object File extends File {
    def saveFile(folder: String, filename: String, id: String, bytes: Array[Byte]): Path = {
      val dir = new java.io.File(folder)
      if(!dir.exists) dir.mkdirs()
      Files.write(Paths.get(s"$folder/$filename"), bytes)
    }
  }
}
