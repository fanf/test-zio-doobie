import cats.effect._
import doobie._
import doobie.implicits._
import zio._
import zio.blocking._
import zio.interop.catz._

/*
 * utilitary base object that provides a query
 */
object BasePure {
  val xaEffect = for {
    ds <- ZIO.accessM[Blocking](_.get.effectBlocking(DatasourceGen()))
    // our transaction EC: wait for aquire/release connections, must accept blocking operations
    te <- ZIO.access[Blocking](_.get.blockingExecutor.asEC)
  } yield {
    Transactor.fromDataSource[Task](ds, te, Blocker.liftExecutionContext(te))
  }

  def queryString(xa: Transactor[Task], sql: Fragment) = sql.query[String].to[List].transact(xa)
}

object TestPureApp {
  val prog = (xa: Transactor[Task]) => for {
    concurrent <- ZIO.foreachParN(20)(List.range(0, 100)){ i =>
                    for {
                      r <- zio.random.nextInt
                      _ <- Log(s"${i}: starting")
                      _ <- BasePure.queryString(xa, sql"select nodeid from nodeconfigurations, pg_sleep(${r%5})")
                      _ <- Log(s"${i}: done")
                    } yield i.toString
                  }
  } yield {
    concurrent
  }


  def main(args: Array[String]): Unit = {
    println(runIO(BasePure.xaEffect.flatMap(xa => prog(xa))))
  }
}
