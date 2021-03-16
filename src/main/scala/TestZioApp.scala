import cats.effect._
import doobie._
import doobie.implicits._
import zio._
import zio.blocking._
import zio.interop.catz._

import java.io.Closeable
import javax.sql.DataSource


/*
 * query module
 */

object DatasourceZio {
  type DatasourceZio = Has[DatasourceZio.Service]
  trait Service {
    def getDS(): UIO[DataSource with Closeable]
  }

  val hikariDS: ZLayer[ZEnv, Throwable, DatasourceZio] = {
    ZLayer.fromAcquireRelease(for {
      _  <- Log(s"********* Initialisation of datasource *********")
      ds <- ZIO.accessM[Blocking](_.get.effectBlocking(DatasourceGen()))
    } yield {
      new Service {
        override def getDS()= ZIO.succeed(ds)
      }
    })(_.getDS().flatMap(ds =>
      Log(s"********* Closing datasource *********") *>
      ZIO.effect(ds.close()).catchAll(ex => Log(s"Exception when closing datasource: ${ex.getMessage}"))
    ))
  }
}

object BaseZio {
  import DatasourceZio._

  type BaseZio = Has[BaseZio.Service]

  trait Service {
    def queryString(sql: Fragment): Task[List[String]]
  }

  val hikariBase: ZLayer[ZEnv with DatasourceZio, Throwable, BaseZio] = {
    ZLayer.fromEffect(for {
      ds <- ZIO.accessM[DatasourceZio](_.get.getDS())
      // our transaction EC: wait for aquire/release connections, must accept blocking operations
      te <- ZIO.access[Blocking](_.get.blockingExecutor.asEC)
      _  <- Log(s"********* Initialisation of transactor *********")
    } yield {
      val xa = Transactor.fromDataSource[Task](ds, te, Blocker.liftExecutionContext(te))
      new Service {
        override def queryString(sql: doobie.Fragment): Task[List[String]] = sql.query[String].to[List].transact(xa)
      }
    })
  }

}


object TestZioApp {
  import BaseZio.BaseZio

  val prog = for {
    concurrent <- ZIO.foreachParN(20)(List.range(0, 100)){ i =>
                    for {
                      r <- zio.random.nextInt
                      _ <- Log(s"${i}: starting")
                      _ <- ZIO.accessM[BaseZio](_.get.queryString(sql"select nodeid from nodeconfigurations, pg_sleep(${r%5})"))
                      _ <- Log(s"${i}: done")
                    } yield i.toString
                  }
  } yield {
    concurrent
  }

  def run[A](effect: ZIO[ZEnv with BaseZio, Throwable, A]) = {
    val all = ZEnv.any >+> DatasourceZio.hikariDS >+> BaseZio.hikariBase
    zio.Runtime.global.unsafeRunSync(effect.provideLayer(all))
  }

  def main(args: Array[String]): Unit = {
    println(run(prog))
  }
}
