package testutils

import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.typesafe.config.ConfigFactory
import org.testcontainers.containers.PostgreSQLContainer
import play.api.db.evolutions.Evolutions
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

case class DBTestingService() extends AutoCloseable {
  import play.api.db.Databases
  import play.api.db.Database

  private val username = "postgres"
  private val password = "secret"
  private val dbName = "my_db"
  private val name = "default"
  private val driver = "org.postgresql.Driver"

  private val postgres: PostgreSQLContainer[_] = {
    val container = new PostgreSQLContainer("postgres:11.1")
    container.withDatabaseName(dbName)
    container.withUsername(username)
    container.withPassword(password)
    container.start()
    container
  }

  private val config =
    s"""
       |slick-postgres {
       |  profile = "slick.jdbc.PostgresProfile$$"
       |  db {
       |    connectionPool = disabled
       |    dataSourceClass = "slick.jdbc.DriverDataSource"
       |    properties = {
       |      driver = "${driver}"
       |      url = "jdbc:postgresql://127.0.0.1:${postgres.getMappedPort(5432)}/${dbName}"
       |      user = "${username}"
       |      password = "${password}"
       |    }
       |  }
       |}
       |""".stripMargin

  startAndRunMigrations()

  private def startAndRunMigrations() = {
    withMyDatabase(database => {
      Evolutions.applyEvolutions(database)
    })
  }

  private def withMyDatabase[T](block: Database => T) = {
    Databases.withDatabase(
      driver = driver,
      url = s"jdbc:postgresql://127.0.0.1:${postgres.getMappedPort(5432)}/${dbName}",
      name = name,
      config = Map(
        "username" -> username,
        "password" -> password,
      )
    )(block)
  }

  def withSlickSession[T](block: SlickSession => T) = {
    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("slick-postgres", ConfigFactory.parseString(config))
    val session = SlickSession.forConfig(databaseConfig)
    try {
      block(session)
    } finally {
      session.close()
    }
  }

  override def close(): Unit = {
    postgres.stop()
  }
}
