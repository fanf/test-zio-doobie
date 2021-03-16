
## Using Doobie with ZIO, but without deadlocks

Lots of users have difficulties to get Doobie configuration right with ZIO regarding concurrency. 
I think that I got a working version, and so I share it

The set up use a datasource for Hikari. I configured here only 2 connection in the pool, and a very long (1 minute)
timeout on the connection aquisition time (ie, the time a fiber will wait in front of the pool for a free connection
before the connection is available).

The test are just doing a sime `select` query on a table with lots of inputs and a 
string column, and I added a random sleep (0~5s) in the postgress side to make it looks more a real query. 

There are 100 queries done in parallel (but of course, only two can be processed at the same time, given the pool config).

In all cases, we initiate the connection pool and only one transactor at the begining of the app. Each query is commited
seperatly in its own transaction (yes, I know, they are only select. The idea would be the same with insert/update).


There is three example: 

- mixed App is what you want to use if you have an app with some part pure, and some other imperative - typically what you
  get when you are porting an old app to ZIO. It shows how to build a 

- a pure are that flatMap things and exec them only once for the whole program, 

- a ZIO app, which is a pure app built with the ZIO `Layer` pattern. 

More information can be found on that stackoverflow question: https://stackoverflow.com/questions/64371510/experiencing-deadlocks-when-using-the-hikari-transactor-for-doobie-with-zio

## Typical ouput for the ZIO based example

```
/..../java/jdk-11.0.6+10/bin/java .... TestZioApp
JAVA INIT: check if should limit number of thread for JVM
=> not limiting threads
[2021-03-16T14:56:53.845] ********* Initialisation of datasource *********
14:56:54.020 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - HikariPool-1 - configuration:
14:56:54.028 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - allowPoolSuspension.............false
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - autoCommit......................false
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - catalog.........................none
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - connectionInitSql...............none
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - connectionTestQuery.............none
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - connectionTimeout...............60000
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - dataSource......................none
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceClassName.............none
14:56:54.029 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceJNDI..................none
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceProperties............{password=<masked>}
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - driverClassName.................none
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - exceptionOverrideClassName......none
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckProperties...........{}
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckRegistry.............none
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - idleTimeout.....................600000
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailTimeout.......1
14:56:54.030 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - isolateInternalQueries..........false
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - jdbcUrl.........................jdbc:postgresql://localhost:15432/rudder
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - leakDetectionThreshold..........0
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - maxLifetime.....................1800000
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - maximumPoolSize.................2
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - metricRegistry..................none
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - metricsTrackerFactory...........none
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - minimumIdle.....................2
14:56:54.031 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - password........................<masked>
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - poolName........................"HikariPool-1"
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - readOnly........................false
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - registerMbeans..................false
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutor...............none
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - schema..........................none
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - threadFactory...................internal
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - transactionIsolation............default
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - username........................"rudder"
14:56:54.032 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.HikariConfig - validationTimeout...............5000
14:56:54.033 [zio-default-blocking-1] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
14:56:54.043 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.util.DriverDataSource - Loaded driver with class name org.postgresql.Driver for jdbcUrl=jdbc:postgresql://localhost:15432/rudder
14:56:54.125 [zio-default-blocking-1] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@7fe0119e
14:56:54.126 [zio-default-blocking-1] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
[2021-03-16T14:56:54.173] ********* Initialisation of transactor *********
14:56:54.226 [HikariPool-1 housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Pool stats (total=1, active=0, idle=1, waiting=0)
14:56:54.232 [HikariPool-1 connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@4b934519
14:56:54.232 [HikariPool-1 connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - After adding stats (total=2, active=0, idle=2, waiting=0)
[2021-03-16T14:56:55.497] 2: starting
[2021-03-16T14:56:55.497] 1: starting
[2021-03-16T14:56:55.498] 8: starting
[2021-03-16T14:56:55.498] 10: starting
[2021-03-16T14:56:55.498] 11: starting
[2021-03-16T14:56:55.499] 12: starting
[2021-03-16T14:56:55.499] 13: starting
[2021-03-16T14:56:55.499] 14: starting
[2021-03-16T14:56:55.499] 15: starting
[2021-03-16T14:56:55.5] 16: starting
[2021-03-16T14:56:55.5] 4: starting
[2021-03-16T14:56:55.5] 17: starting
[2021-03-16T14:56:55.5] 19: starting
[2021-03-16T14:56:55.501] 6: starting
[2021-03-16T14:56:55.497] 7: starting
[2021-03-16T14:56:55.498] 9: starting
[2021-03-16T14:56:55.501] 5: starting
[2021-03-16T14:56:55.497] 0: starting
[2021-03-16T14:56:55.5] 18: starting
[2021-03-16T14:56:55.507] 3: starting
[2021-03-16T14:56:55.672] 11: done
[2021-03-16T14:56:55.673] 20: starting
[2021-03-16T14:56:55.687] 7: done
[2021-03-16T14:56:55.688] 21: starting
[2021-03-16T14:56:57.709] 18: done
[2021-03-16T14:56:57.71] 22: starting
[2021-03-16T14:56:57.722] 17: done
[2021-03-16T14:56:57.723] 23: starting
[2021-03-16T14:56:58.647] 15: done
[2021-03-16T14:56:58.648] 24: starting
[2021-03-16T14:56:58.662] 5: done
[2021-03-16T14:56:58.663] 25: starting
[2021-03-16T14:56:58.67] 6: done
[2021-03-16T14:56:58.671] 26: starting
[2021-03-16T14:57:01.752] 1: done
[2021-03-16T14:57:01.753] 27: starting
[2021-03-16T14:57:01.775] 2: done
[2021-03-16T14:57:01.776] 28: starting
[2021-03-16T14:57:02.704] 3: done
[2021-03-16T14:57:02.705] 29: starting
[2021-03-16T14:57:02.724] 4: done
[2021-03-16T14:57:02.725] 30: starting
[2021-03-16T14:57:02.738] 14: done
[2021-03-16T14:57:02.738] 31: starting
[2021-03-16T14:57:02.747] 0: done
[2021-03-16T14:57:02.747] 32: starting
[2021-03-16T14:57:02.757] 10: done
[2021-03-16T14:57:02.758] 33: starting
[2021-03-16T14:57:05.778] 16: done
[2021-03-16T14:57:05.779] 34: starting
[2021-03-16T14:57:05.792] 8: done
[2021-03-16T14:57:05.792] 35: starting
[2021-03-16T14:57:05.797] 9: done
[2021-03-16T14:57:05.798] 36: starting
[2021-03-16T14:57:05.802] 13: done
[2021-03-16T14:57:05.803] 37: starting
[2021-03-16T14:57:05.809] 19: done
[2021-03-16T14:57:05.81] 38: starting
[2021-03-16T14:57:07.811] 12: done
[2021-03-16T14:57:07.811] 39: starting
[2021-03-16T14:57:07.823] 21: done
[2021-03-16T14:57:07.824] 40: starting
[2021-03-16T14:57:07.832] 22: done
[2021-03-16T14:57:07.833] 41: starting
[2021-03-16T14:57:09.822] 20: done
[2021-03-16T14:57:09.822] 42: starting
[2021-03-16T14:57:09.831] 24: done
[2021-03-16T14:57:09.832] 43: starting
[2021-03-16T14:57:09.842] 25: done
[2021-03-16T14:57:09.843] 44: starting
[2021-03-16T14:57:10.843] 23: done
[2021-03-16T14:57:10.844] 45: starting
[2021-03-16T14:57:11.852] 27: done
[2021-03-16T14:57:11.852] 46: starting
[2021-03-16T14:57:11.862] 28: done
[2021-03-16T14:57:11.863] 47: starting
[2021-03-16T14:57:11.873] 29: done
[2021-03-16T14:57:11.874] 48: starting
[2021-03-16T14:57:12.867] 26: done
[2021-03-16T14:57:12.868] 49: starting
[2021-03-16T14:57:12.877] 31: done
[2021-03-16T14:57:12.877] 50: starting
[2021-03-16T14:57:14.901] 30: done
[2021-03-16T14:57:14.901] 51: starting
[2021-03-16T14:57:14.903] 32: done
[2021-03-16T14:57:14.904] 52: starting
[2021-03-16T14:57:14.914] 33: done
[2021-03-16T14:57:14.914] 53: starting
[2021-03-16T14:57:14.917] 34: done
[2021-03-16T14:57:14.918] 54: starting
[2021-03-16T14:57:14.922] 35: done
[2021-03-16T14:57:14.922] 55: starting
[2021-03-16T14:57:14.93] 37: done
[2021-03-16T14:57:14.93] 56: starting
[2021-03-16T14:57:14.939] 38: done
[2021-03-16T14:57:14.939] 57: starting
[2021-03-16T14:57:17.938] 36: done
[2021-03-16T14:57:17.939] 58: starting
[2021-03-16T14:57:17.96] 39: done
[2021-03-16T14:57:17.96] 59: starting
[2021-03-16T14:57:18.97] 41: done
[2021-03-16T14:57:18.97] 60: starting
[2021-03-16T14:57:20.96] 40: done
[2021-03-16T14:57:20.96] 61: starting
[2021-03-16T14:57:22] 43: done
[2021-03-16T14:57:22.001] 62: starting
[2021-03-16T14:57:22.023] 42: done
[2021-03-16T14:57:22.025] 63: starting
[2021-03-16T14:57:22.051] 44: done
[2021-03-16T14:57:22.054] 64: starting
[2021-03-16T14:57:22.072] 45: done
[2021-03-16T14:57:22.072] 65: starting
[2021-03-16T14:57:22.082] 46: done
[2021-03-16T14:57:22.083] 66: starting
[2021-03-16T14:57:22.094] 47: done
[2021-03-16T14:57:22.094] 67: starting
[2021-03-16T14:57:22.096] 48: done
[2021-03-16T14:57:22.096] 68: starting
[2021-03-16T14:57:22.109] 50: done
[2021-03-16T14:57:22.109] 69: starting
[2021-03-16T14:57:22.116] 51: done
[2021-03-16T14:57:22.117] 70: starting
[2021-03-16T14:57:24.107] 49: done
[2021-03-16T14:57:24.108] 71: starting
[2021-03-16T14:57:24.115] 53: done
[2021-03-16T14:57:24.116] 72: starting
14:57:24.227 [HikariPool-1 housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Pool stats (total=2, active=2, idle=0, waiting=18)
14:57:24.227 [HikariPool-1 housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Fill pool skipped, pool is at sufficient level.
[2021-03-16T14:57:25.139] 52: done
[2021-03-16T14:57:25.14] 73: starting
[2021-03-16T14:57:25.16] 55: done
[2021-03-16T14:57:25.16] 74: starting
[2021-03-16T14:57:25.171] 56: done
[2021-03-16T14:57:25.172] 75: starting
[2021-03-16T14:57:25.18] 57: done
[2021-03-16T14:57:25.18] 76: starting
[2021-03-16T14:57:25.187] 58: done
[2021-03-16T14:57:25.187] 77: starting
[2021-03-16T14:57:25.193] 59: done
[2021-03-16T14:57:25.194] 78: starting
[2021-03-16T14:57:26.214] 60: done
[2021-03-16T14:57:26.215] 79: starting
[2021-03-16T14:57:26.234] 61: done
[2021-03-16T14:57:26.234] 80: starting
[2021-03-16T14:57:26.244] 62: done
[2021-03-16T14:57:26.244] 81: starting
[2021-03-16T14:57:26.252] 63: done
[2021-03-16T14:57:26.253] 82: starting
[2021-03-16T14:57:27.265] 64: done
[2021-03-16T14:57:27.266] 83: starting
[2021-03-16T14:57:27.275] 65: done
[2021-03-16T14:57:27.275] 84: starting
[2021-03-16T14:57:27.283] 66: done
[2021-03-16T14:57:27.283] 85: starting
[2021-03-16T14:57:28.141] 54: done
[2021-03-16T14:57:28.142] 86: starting
[2021-03-16T14:57:28.303] 67: done
[2021-03-16T14:57:28.305] 87: starting
[2021-03-16T14:57:28.324] 69: done
[2021-03-16T14:57:28.325] 88: starting
[2021-03-16T14:57:28.343] 70: done
[2021-03-16T14:57:28.344] 89: starting
[2021-03-16T14:57:28.357] 71: done
[2021-03-16T14:57:28.357] 90: starting
[2021-03-16T14:57:28.364] 72: done
[2021-03-16T14:57:28.365] 91: starting
[2021-03-16T14:57:31.163] 68: done
[2021-03-16T14:57:31.164] 92: starting
[2021-03-16T14:57:31.185] 74: done
[2021-03-16T14:57:31.185] 93: starting
[2021-03-16T14:57:31.199] 75: done
[2021-03-16T14:57:31.199] 94: starting
[2021-03-16T14:57:31.406] 73: done
[2021-03-16T14:57:31.407] 95: starting
[2021-03-16T14:57:34.222] 76: done
[2021-03-16T14:57:34.223] 96: starting
[2021-03-16T14:57:34.43] 77: done
[2021-03-16T14:57:34.431] 97: starting
[2021-03-16T14:57:34.453] 79: done
[2021-03-16T14:57:34.454] 98: starting
[2021-03-16T14:57:36.238] 78: done
[2021-03-16T14:57:36.238] 99: starting
[2021-03-16T14:57:36.251] 81: done
[2021-03-16T14:57:37.48] 80: done
[2021-03-16T14:57:37.501] 83: done
[2021-03-16T14:57:37.512] 84: done
[2021-03-16T14:57:37.52] 85: done
[2021-03-16T14:57:37.527] 86: done
[2021-03-16T14:57:37.534] 87: done
[2021-03-16T14:57:37.541] 88: done
[2021-03-16T14:57:39.277] 82: done
[2021-03-16T14:57:39.295] 90: done
[2021-03-16T14:57:39.567] 89: done
[2021-03-16T14:57:39.589] 92: done
[2021-03-16T14:57:39.605] 93: done
[2021-03-16T14:57:40.627] 94: done
[2021-03-16T14:57:40.65] 95: done
[2021-03-16T14:57:40.66] 96: done
[2021-03-16T14:57:41.667] 97: done
[2021-03-16T14:57:41.674] 98: done
[2021-03-16T14:57:41.68] 99: done
[2021-03-16T14:57:42.323] 91: done
[2021-03-16T14:57:42.498] ********* Closing datasource *********
14:57:42.500 [scala-execution-context-global-23] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
14:57:42.501 [scala-execution-context-global-23] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Before shutdown stats (total=2, active=0, idle=2, waiting=0)
14:57:42.502 [HikariPool-1 connection closer] DEBUG com.zaxxer.hikari.pool.PoolBase - HikariPool-1 - Closing connection org.postgresql.jdbc.PgConnection@7fe0119e: (connection evicted)
14:57:42.503 [HikariPool-1 connection closer] DEBUG com.zaxxer.hikari.pool.PoolBase - HikariPool-1 - Closing connection org.postgresql.jdbc.PgConnection@4b934519: (connection evicted)
14:57:42.504 [scala-execution-context-global-23] DEBUG com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - After shutdown stats (total=0, active=0, idle=0, waiting=0)
14:57:42.504 [scala-execution-context-global-23] INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown completed.
Success(List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99))

Process finished with exit code 0
```