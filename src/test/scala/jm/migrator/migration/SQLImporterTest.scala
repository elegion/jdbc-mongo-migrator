package jm.migrator.migration

import org.scalatest.Spec
import jm.migrator.parser.MappingParser
import java.sql.{DriverManager, Connection}
import net.lag.configgy.Configgy
import org.scalatest.matchers.MustMatchers
import jm.migrator.db.DBUtil._
import jm.migrator.db.InsertBackend
import com.mongodb.DBObject

import com.mongodb.casbah.Imports._

;


/*
 * Author: Yuri Buyanov
 * Date: 3/23/11 2:52 PM
 */
trait TestInsertBackend extends InsertBackend {
  import scala.collection.mutable.Map
  val objMap: Map[String, List[DBObject]] = Map.empty


  def doInsert = doInsertInternal

  def doInsertInternal(objects: Seq[DBObject], collectionName: String) = {
    objMap += collectionName ->
                 (objMap.get(collectionName).getOrElse(List.empty) ++ objects)
  }

  def getInserts(collectionName: String) = {
    objMap get collectionName getOrElse List.empty
  }
}

class SQLImporterTest extends Spec with MustMatchers {
  val config = getClass.getResource("/test_config.conf").getFile
  Configgy.configure(config)

  describe("Using test db and SQLImporter") {
    Class.forName(Configgy.config.getString("jdbc.driver", ""));
    using (DriverManager.getConnection(Configgy.config.getString("jdbc.uri",  ""))) { conn =>

      val script = getClass.getResource("/test_db.sql").getFile

      conn.createStatement.executeUpdate("RUNSCRIPT FROM '"+script+"'") must not be (0)
      val rs = conn.createStatement.executeQuery("SELECT COUNT (*) FROM users AS u")

      val parser = new MappingParser
      val mapping = parser.parseFile(getClass.getResource("/test_mapping.json").getFile)
      val importer = new SQLImporter(mapping) with TestInsertBackend
      it ("Should fetch data") {
        val fetchResult = importer.fetch
        val userInserts = importer.getInserts("users")
        userInserts must have size(5)
        val user0 = userInserts(0)
        user0.getAs[String]("username") must equal (Some("user0"))
        user0.expand[Int]("counters.rating") must equal (Some(0))

        //TODO: further tests
      }
    }

  }

}