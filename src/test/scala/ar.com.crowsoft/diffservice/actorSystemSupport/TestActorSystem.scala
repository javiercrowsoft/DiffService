package ar.com.crowsoft.diffservice.actorSystemSupport

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import ar.com.crowsoft.diffservice.util.lang._

object TestActorSystem {
  def apply(name: String, config: Config, useFileFallback: Boolean = true) = {

    val cc = config.withFallback(ConfigFactory.parseResources("base.conf"))
    val systemConfig = useFileFallback.fold(cc){ _ => cc withFallback(ConfigFactory.load()) }

    ActorSystem(name, systemConfig.resolve())
  }
}
