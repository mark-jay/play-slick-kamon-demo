package modules

import actors.CounterActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MainModule extends AbstractModule with AkkaGuiceSupport {
  override def configure = {
    bindActor[CounterActor]("counter-actor")
  }
}
