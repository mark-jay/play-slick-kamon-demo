package actors

import akka.actor._
import javax.inject._
import play.api.Configuration

class CounterActor @Inject() (configuration: Configuration) extends Actor {
  var counter = 0

  override def receive: Receive = {
    case "count" => {
      counter += 1
      sender() ! s"${counter}"
    }
  }
}
