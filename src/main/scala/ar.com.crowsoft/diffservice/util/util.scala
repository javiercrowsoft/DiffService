package ar.com.crowsoft.diffservice

package object util {

  object lang {

    implicit def bool2Opt(b: Boolean): Option[Boolean] = {
      b match {
        case true => Some(true)
        case _ => None
      }
    }
  }
}