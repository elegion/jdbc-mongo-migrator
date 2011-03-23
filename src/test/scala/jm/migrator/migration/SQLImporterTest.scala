package jm.migrator.migration

import org.scalatest.Spec
import jm.migrator.parser.MappingParser
import java.sql.{DriverManager, Connection}
import net.lag.configgy.Configgy
import org.scalatest.matchers.MustMatchers
import jm.migrator.db.DBUtil._
;


/*
 * Author: Yuri Buyanov
 * Date: 3/23/11 2:52 PM
 */
class SQLImporterTest extends Spec with MustMatchers {
  val config = getClass.getResource("/test_config.conf").getFile
  Configgy.configure(config)

  describe("Using test db and SQLImporter") {
    Class.forName(Configgy.config.getString("jdbc.driver", ""));
    using (DriverManager.getConnection(Configgy.config.getString("jdbc.uri",  ""))) { conn =>

      val script = getClass.getResource("/test_db.sql").getFile

      conn.createStatement.executeUpdate("RUNSCRIPT FROM '"+script+"'") must not be (0)
      val rs = conn.createStatement.executeQuery("SELECT COUNT (*) FROM users AS u")
      rs.next()
      rs.getInt(1) must be (3)

      val parser = new MappingParser
      val mapping = parser.parseFile(getClass.getResource("/test_mapping.json").getFile)
      val importer = new SQLImporter(mapping)
      it ("Should fetch data") {
        val fetchResult = importer.fetch
      }
    }

  }

}