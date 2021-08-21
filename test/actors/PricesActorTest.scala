package actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json._
import play.api.mvc._
import play.api.routing.sird._
import play.api.test.Injecting
import play.core.server.Server

import scala.concurrent.duration.{FiniteDuration, _}


class PricesActorTest extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "PricesActor" should {
    "return valid text" in {
      val body =
        """
          |{"lastUpdateId":13151736580,"bids":[["48858.38000000","1.61188800"],["48858.36000000","0.25192500"],["48858.34000000","0.23588300"],["48854.84000000","0.22309400"],["48854.83000000","0.31426200"]],"asks":[["48858.39000000","2.89640600"],["48860.01000000","0.47333500"],["48862.52000000","0.24247200"],["48864.55000000","0.00136800"],["48864.60000000","0.01000000"]]}
          |""".stripMargin
      Server.withRouterFromComponents() { components =>
        import Results._
        import components.{defaultActionBuilder => Action}
        {
          case GET(p"/api/v3/depth") =>
            Action {
              Ok(Json.parse(body))
            }
        }
      } { implicit port =>

        implicit val system = ActorSystem("test-system")

        implicit val timeout: Timeout = 30.seconds
        implicit val duration: FiniteDuration = 30.seconds

        val pricesActorRef = system.actorOf(Props(new PricesActor(s"localhost", port.value, false)))
        val probe = TestProbe()
        probe.send(pricesActorRef, "btc")
        val result = probe.receiveOne(2.seconds)
        println(s"result = ${result}")
        result mustBe """price = 48858.39"""
      }
    }
  }

}
