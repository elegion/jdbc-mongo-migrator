package jm.migrator.migration

import jm.migrator.db.DBUtil._

import net.lag.logging.Logger
import net.lag.configgy.Configgy
import jm.migrator.domain._
import collection.mutable.Buffer
import com.mongodb.casbah.Imports._
import jm.migrator.db.MongoUtil._
import java.sql.{Statement, Connection, ResultSetMetaData, ResultSet}

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
  val config = Configgy.config
  val limit = config.getInt("jdbc.limit", 0)
  def fetch = {

    using(connection) { conn =>
      mapping map { collectionMapping =>
        var offset = 0
        var fetched = -1
        var fetchedTotal = 0
        log.debug("=== db."+collectionMapping.name+" inserts: ===")
        do {
          fetched = using(conn createStatement ) { stmt =>
                          query(
                            collectionMapping,
                            stmt,
                            limit,
                            offset
                          )
          }
          offset += limit
          fetchedTotal += fetched
        } while (limit!=0 && fetched!=0)
        log.info("%d entries imported to %s", fetchedTotal, collectionMapping.name)
      }
    }
  }

  def query(collectionMapping: CollectionMapping,
            stmt: Statement,
            limit: Int,
            offset: Int): Int = {
    val SQL = collectionMapping.toSQL()
    val pagedSQL = SQL + (limit match {
      case 0 => ""
      case lim => " LIMIT "+lim+" OFFSET "+offset
    })
    log.debug("SQL: "+pagedSQL)

    using (stmt executeQuery pagedSQL) { rs: ResultSet =>
      val flatValuesMaps = process(rs, collectionMapping)
      val insert = flatValuesMaps map { fieldmap =>
        val map = clusterByPrefix(fieldmap).toMap
        val b = MongoDBObject.newBuilder
        b ++= map
        b.result
      }
      doInsert(insert, collectionMapping.name)
      flatValuesMaps.size
    }
  }


  def processSub(rs: ResultSet, mapping: MappedValue): Seq[Any] = {
    val buffer = Buffer[Any]()
    while (rs.next) {
//      val rsValue = rs.getObject(1)
      val value = mapping match {
        case mc: MappedColumn => mc.toValue(rs.getObject(1))
        case Fields(fieldmap) => {
          val pairs = fieldmap.zipWithIndex map {
            case ((fieldName, mappedColumn), index) =>
              (fieldName, mappedColumn.asInstanceOf[MappedColumn] toValue rs.getObject(index+1))
          }
          MongoDBObject(pairs.toSeq: _*)
        }
        case _ => rs.getObject(1)
      }
      buffer += value
    }
    buffer
  }

  def process(rs: ResultSet, collectionMapping: CollectionMapping): Seq[Map[String, Any]] = {
    val buffer = Buffer[Map[String, Any]]()
    import scala.collection.mutable.Map
    val columnFieldNames = collectionMapping.mapping.fields.collect{
      case (name, col: MappedColumn) => (name, col)
    }

    while (rs.next) {
      val map = Map[String, Any]()
      //collect simple values from resultset
      columnFieldNames.zipWithIndex foreach {
        case ((fieldName, mappedColumn), index) =>
          val rsValue = rs.getObject(index+1)
          val value = mappedColumn toValue rsValue
          if (value!=null) {
            map.put(fieldName, value)
          }
      }
      //execute subselects
      collectionMapping.mapping.fields collect {
        case (name, a: Array) => {
          log.debug("SUB SELECT: "+ a.toSQL(map.toMap))
          using(rs.getStatement.getConnection.createStatement) { stmt =>
            using (stmt executeQuery (a toSQL map.toMap)) { rs =>
              val arr = processSub(rs, a.mapping)
              map.put(name, arr)
            }
          }
        }
        case (name, c: Count) => {
          log.debug("SUB SELECT: "+ c.toSQL(map.toMap))
          using(rs.getStatement.getConnection.createStatement) { stmt =>
            using (stmt executeQuery (c toSQL map.toMap)) { rs =>
              val count = if (rs.first()) rs.getInt(1) else 0
              map.put(name, count)
            }
          }
        }
        case (name, c: CountMap) => {
          log.debug("SUB SELECT: "+ c.toSQL(map.toMap))
          using(rs.getStatement.getConnection.createStatement) { stmt =>
            using (stmt executeQuery (c toSQL map.toMap)) { rs =>
              if (rs.next) {
                val countMap = MongoDBObject.newBuilder
                do {
                  log.debug("countMap %s.%s = %d", name , rs.getString(1), rs.getInt(2))
                  countMap += (rs.getString(1) -> rs.getInt(2))
                } while(rs.next)
                map.put(name, countMap.result)
              }
            }
          }
        }
      }


      buffer += map.toMap
    }
    buffer.toSeq
  }

}