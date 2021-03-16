import cats.effect._
import doobie._
import doobie.implicits._
import zio._
import zio.blocking._
import zio.interop.catz._

object runIO {
  def apply[E, A](io: ZIO[ZEnv, E, A]): A = {
    zio.Runtime.global.unsafeRun(io)
  }
}

/*
 * utilitary base object that provides a query
 */
object Base {
  val datasource = DatasourceGen()
  val xaEffect = for {
    // our transaction EC: wait for aquire/release connections, must accept blocking operations
    te <- ZIO.access[Blocking](_.get.blockingExecutor.asEC)
  } yield {
    Transactor.fromDataSource[Task](datasource, te, Blocker.liftExecutionContext(te))
  }
  // our global transactor is instanciated only once at the begining of the app
  val xa = runIO(xaEffect)

  def transactTask[T](query: Transactor[Task] => Task[T]): Task[T] = {
    query(xa)
  }

  def queryString(sql: Fragment) = transactTask(xa => sql.query[String].to[List].transact(xa))
}

object Example {
  val prog = for {
    concurrent <- ZIO.foreachParN(20)(List.range(0, 100)){ i =>
                    for {
                      r <- zio.random.nextInt
                      _ <- Log(s"${i}: starting")
                      _ <- Base.queryString(sql"select nodeid from nodeconfigurations, pg_sleep(${r%5})")
                      _ <- Log(s"${i}: done")
                    } yield i.toString
                  }
  } yield {
    concurrent
  }


  def main(args: Array[String]): Unit = {
    println(runIO(prog))
  }
}
