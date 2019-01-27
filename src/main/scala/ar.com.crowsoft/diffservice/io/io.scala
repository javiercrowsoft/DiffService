package ar.com.crowsoft.diffservice

import java.nio.file.{Files, Path, Paths}

import ar.com.crowsoft.diffservice.io.DiffActor.DiffDetail
import ar.com.crowsoft.diffservice.util.lang._

package object io {

  trait FileSide {
    def name: String
    override def toString: String = name
  }

  case class LeftFile() extends FileSide {
    val name = "LEFT"
  }

  case class RightFile() extends FileSide {
    val name = "RIGHT"
  }

  trait File {

    def saveFile(folder:String, filename: String, bytes: Array[Byte]): Path

    def diffFiles(filenameLeft: String, filenameRight: String): CompareInfo
  }

  case class CompareResult(code: Int, description: String)

  val Identical = CompareResult(304, "The files are identical")
  val SizeNotEqual = CompareResult(409, "The files's size aren't equal")

  val BothMissing = CompareResult(600, "Bothf files are missing")
  val LeftMissing = CompareResult(601, "Left file is missing")
  val RightMissing = CompareResult(602, "Right file is missing")

  val MisingFiles = List(BothMissing, LeftMissing, RightMissing)

  def isMissingCode(code: Int) = MisingFiles.exists(cr => cr.code == code)

  case class CompareInfo(result: CompareResult, diffs: List[DiffDetail] = List())

  object File extends File {

    def saveFile(folder: String, filename: String, bytes: Array[Byte]): Path = {
      val dir = new java.io.File(folder)
      if(!dir.exists) dir.mkdirs()
      Files.write(Paths.get(s"$folder/$filename"), bytes)
    }

    def diffFiles(filenameLeft: String, filenameRight: String): CompareInfo = {
      val fileLeft = new java.io.File(filenameLeft)
      val fileRight = new java.io.File(filenameRight)

      def sameSizes = fileLeft.length() == fileRight.length()
      def compare = {
        CompareInfo(Identical)
      }

      (fileLeft.exists(), fileRight.exists()) match {
        case (true, true) => sameSizes.fold(CompareInfo(SizeNotEqual)){ _ => compare }
        case (true, _) => CompareInfo(RightMissing)
        case _ => CompareInfo(BothMissing)
      }
    }
  }
}
