package actors

import akka.{Done, NotUsed}
import akka.actor._
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Flow, Sink, Source}
import javax.inject._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import services.SlickSessionHolder
import slick.dbio.Effect
import slick.jdbc.JdbcProfile
import slick.sql.SqlStreamingAction

import scala.concurrent.Future

// This import enables the use of the Slick sql"...",
// sqlu"...", and sqlt"..." String interpolators.
// See "http://slick.lightbend.com/doc/3.2.1/sql.html#string-interpolation"
//import session.profile.api._
import slick.jdbc.H2Profile.api._


class CounterActor @Inject() (
                               sessionHolder: SlickSessionHolder
                             ) extends Actor {

  implicit val session: SlickSession = sessionHolder.session
  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case "count" => {
      val actor = sender()
      val done: Future[Done] =
        Source.single("1")
          .via(Slick.flowWithPassThrough { (request: String) =>
            sql"SELECT MAX(COUNT) FROM COUNTER".as[Int]
          })
          .via(Slick.flowWithPassThrough { count =>
            val newCount = if (count.isEmpty) 1 else (count(0) + 1)
            (sqlu"INSERT INTO COUNTER(count) VALUES(${newCount})")
              .map(result => (newCount, result))
          })
          .map(item => {
            val (newCount, result) = item
            actor ! s"${newCount}(updated ${result})"
          })
          .log("nr-of-updated-rows")
//          .to(Sink.ignore)
          .run()
    }
  }
}
