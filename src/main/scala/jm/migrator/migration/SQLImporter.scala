package jm.migrator.migration

import jm.migrator.db.DBUtil._

import net.lag.logging.Logger
import java.sql.{ResultSetMetaData, ResultSet}
import jm.migrator.domain._
import collection.mutable.Buffer
import com.mongodb.casbah.Imports._
import jm.migrator.db.MongoUtil._

/**
 * Authod: Yuri Buyanov
 * Date: 2/4/11 11:52 AM
 */

case class ColumnData(name: String, columnType: Int, fieldName: String)

case object Meta {
  val log = Logger get

  def apply(rs: ResultSet, collectionMapping: CollectionMapping): Iterator[ColumnData] = new Iterator[ColumnData] {
    val metaData = rs.getMetaData
    val columnFieldNames = collectionMapping.mapping.fields.collect{
      case (name, MappedColumn(_)) => name
    }.toSeq

    var i = 1

    override def hasDefiniteSize = true
    override def size = metaData getColumnCount

    log.debug("size = " + size)

    def hasNext = i < size
    def next = { log.debug("i="+i); val d = toColumnData(metaData, i); i+=1; d }

    def toColumnData(rsMeta: ResultSetMetaData, n: Int) = {
      ColumnData(
        rsMeta getColumnName n,
        rsMeta getColumnType n,
        columnFieldNames(n-1)
      )
    }
  }

}


class SQLImporter(val mapping: Iterable[CollectionMapping] ) {
  val log = Logger get

  def fetch = {
    using(connection) { conn =>
      mapping map { collectionMapping =>

        log.info("=== db."+collectionMapping.name+" inserts: ===")
        using(conn createStatement ) { stmt =>
          using (stmt executeQuery (collectionMapping toSQL)) { rs =>

            val maps = process(rs, collectionMapping)
            val insert = maps foreach { fieldmap =>

              val map = clusterByPrefix(fieldmap).toMap
              val obj: DBObject = map
              log.info("INSERT: "+ obj)
            }

            maps
          }
        }
      }
    }
  }

  def process(rs: ResultSet, collectionMapping: CollectionMapping): Seq[Map[String, Any]] = {
    val buffer = Buffer[Map[String, Any]]()
    import scala.collection.mutable.Map
    val columnFieldNames = collectionMapping.mapping.fields.collect{
      case (name, col: MappedColumn) => (name, col)
    }
    log.debug("columnFieldNames = "+ columnFieldNames)


    while (rs.next) {
      val map = Map[String, Any]()
      columnFieldNames.zipWithIndex foreach {
        case ((fieldName, mappedColumn), index) =>
          val rsValue = rs.getObject(index+1)
          val value = mappedColumn match {
            case MongoId(map) => getMongoId(rsValue)
            case _ => rsValue
          }
          map.put(fieldName, value)
      }
      buffer += map.toMap
    }
    buffer.toSeq
  }

}