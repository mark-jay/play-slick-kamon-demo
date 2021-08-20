package actors

import akka.actor.{ActorSystem, Props}
import akka.stream.alpakka.slick.scaladsl._
import akka.stream.scaladsl._
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import controllers.HomeController
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.testcontainers.containers.PostgreSQLContainer
import play.db.DBApi
import play.api.db.Databases
import play.api.test.Injecting
import play.db.Database
import services.{SimpleSlickSessionHolder, SlickSessionHolder}
import slick.basic.DatabaseConfig
import slick.jdbc.{GetResult, JdbcProfile}
import play.api.db.evolutions._
import testutils.DBTestingService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._


class CounterActorTest extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "CounterActor GET" should {
    "return valid text" in {

      val service = DBTestingService()
      service.withSlickSession(slickSession => {
        implicit val system = ActorSystem("test-system")

        implicit val timeout: Timeout = 30.seconds
        implicit val duration: FiniteDuration = 30.seconds

        val counterActorRef = system.actorOf(Props(new CounterActor(SimpleSlickSessionHolder(slickSession))))
        val probe = TestProbe()
        probe.send(counterActorRef, "count")
        val result = probe.receiveOne(2.seconds)
        println(s"result = ${result}")
        result mustBe """1(updated 1)"""
      })

    }
  }

}
