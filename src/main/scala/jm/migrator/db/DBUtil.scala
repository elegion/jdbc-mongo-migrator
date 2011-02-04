package jm.migrator.db

import java.sql.{Connection, DriverManager, ResultSet}
import net.lag.logging.Logger
import net.lag.configgy.Configgy
//import java.io.Closeable

/**
 * Authod: Yuri Buyanov
 * Date: 2/4/11 11:42 AM
 */

object DBUtil {
  private val config = Configgy config
  private val log = Logger get

  Class.forName(config getString ("jdbc.driver", "com.mysql.jdbc.Driver")).newInstance;
  val connection = DriverManager getConnection (config getString ("jdbc.uri", "jdbc://localhost"))

  def using[Closeable <: {def close(): Unit}, B](closeable: Closeable)(getB: Closeable => B): B =
    try {
      getB(closeable)
    } finally {
      closeable.close()
    }


}