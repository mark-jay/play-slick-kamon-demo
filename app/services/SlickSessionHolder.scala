package services

import akka.actor.ActorSystem
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

@ImplementedBy(classOf[SlickSessionHolderSingleton])
trait SlickSessionHolder {
  def session: SlickSession
}

case class SimpleSlickSessionHolder(session: SlickSession) extends SlickSessionHolder

@Singleton
class SlickSessionHolderSingleton @Inject() (
                                     protected val dbConfigProvider: DatabaseConfigProvider,
                                     system: ActorSystem,
                                   ) extends SlickSessionHolder with HasDatabaseConfigProvider[JdbcProfile] {

  val session: SlickSession = SlickSession.forConfig(dbConfig)
  system.registerOnTermination(() => session.close())
}
