package modules

import actors.{BinancePricesActor, CounterActor, PricesActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MainModule extends AbstractModule with AkkaGuiceSupport {
  override def configure = {
    bindActor[CounterActor]("counter-actor")
    bindActor[BinancePricesActor]("prices-actor")
  }
}
