import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import zio.ZIO
import zio.blocking.Blocking

import java.io.Closeable
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

object DatasourceGen {
  val config = {
    Class.forName("org.postgresql.Driver")

    val config = new HikariConfig()
    config.setJdbcUrl("jdbc:postgresql://localhost:15432/rudder")

    config.setUsername("rudder")
    config.setPassword("rudder")
    config.setMaximumPoolSize(2)
    config.setAutoCommit(false)

    // `connectionTimeout` is the time hikari wait when there is no connection available or base down before telling
    // upward that there is no connection. Long waiting makes app slow to detect problem or handle too many long connection,
    // and we prefer to be notified quickly that it was
    // impossible to get a connection. Must be >=250ms. App must know how to handle that gracefullly.
    //config.setConnectionTimeout(250) // in milliseconds
    config.setConnectionTimeout(60 * 1000) // for test

    // since we use JDBC4 driver, we MUST NOT set `setConnectionTestQuery("SELECT 1")`
    // more over, it causes problems, see: https://issues.rudder.io/issues/14789

    config
  }

  def apply() = {

    val datasource: DataSource with Closeable = try {
      val pool       = new HikariDataSource(config)
      /* try to get the connection to check everything is ok */
      val connection = pool.getConnection()
      connection.close()
      pool
    } catch {
      case e: Exception =>
        println("ERROR - Could not initialise the access to the database")
        throw e
    }
    datasource
  }
}

object Log {
  //log
  def apply(msg: String) = {
    (for {
      t <- ZIO.accessM[zio.clock.Clock](_.get.localDateTime).map(_.format(DateTimeFormatter.ISO_DATE_TIME))
      _ <- ZIO.accessM[Blocking](_.get.effectBlocking(println(s"[${t}] ${msg}")))
    } yield ()).catchAll(ex => throw ex) // if clock and console fails, kill everything
  }
}
