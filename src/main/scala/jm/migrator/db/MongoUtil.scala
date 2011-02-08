package jm.migrator.db

import com.mongodb.casbah.Imports._

import net.lag.logging.Logger
import net.lag.configgy.Configgy

/**
 * Authod: Yuri Buyanov
 * Date: 2/4/11 2:36 PM
 */

object MongoUtil {
  val log = Logger get
  val config = Configgy config

  val dryRun = config.getBool("mongo.dry-run", true)


  val (connOpt, dbOpt) =
    if (dryRun) {
      log.warning("DRY RUN, NO ACTUAL DATA WRITTEN")
      (None, None)
    } else {
      val conn = MongoConnection(
        config.getString("mongo.host", "localhost"),
        config.getInt("mongo.port", 27017))
      log.info("Connected to %s", conn.debugString)

      val db = conn(config.getString("mongo.database", "default"))
      log.info("Using DB %s", db)

      if (config.getBool("mongo.clean", true)) {
        log.info("mongo.clean = true, dropping DB before migration")
        db.dropDatabase
      }

      (Some(conn), Some(db))
    }

  def expandPair(k: String, v: Any): Pair[String, Any] = {
    val names = k.split('.')
    val value = names.tail match {
      case empty if empty.isEmpty => v

      case arr => {
        val innermost: Any = MongoDBObject(arr.last -> v)
        arr.init.foldLeft(innermost) { (value, name) =>
          (name -> value)
        }
      }
    }
    print(names.head, value)
    (names.head, value)
  }

  //unflattens the map according to dot notation
  def clusterByPrefix(fields: Iterable[(String, Any)]): Iterable[(String, Any)] = {
    val result = fields.groupBy( _._1.split('.').head) map { case (k, v) =>
      val firstKey = v.head._1
      val newValue =
        if (firstKey.contains('.')) {
          map2MongoDBObject(clusterByPrefix(v map { p =>
            (p._1 drop p._1.indexOf('.')+1, p._2)
          }).toMap)
        } else {
          v.head._2
        }
      (k, newValue)
    }

    result
  }

  import scala.collection.mutable.Map
  val idCache = Map[(String, Any), ObjectId]()

  def getMongoId(value: Any, collection: String): ObjectId =
    idCache.get(collection, value)
      .map{ id =>
        log.debug("Cache hit for $oid %s in \"%s\"", id.toString, collection)
        id
      }.getOrElse{
        val id = new ObjectId()
        idCache.put((collection, value), id)
        log.debug("Cache miss: created $oid %s in \"%s\"", id.toString, collection)
        id
      }

  def doInsert(objects: Seq[DBObject], collectionName: String) = {
    log.info("Inserting %d entries into \"%s\" collection", objects.size, collectionName)
    objects foreach (o => log.debug(o.toString))
    dbOpt.foreach { db =>
      val collection = db.getCollection(collectionName)
      val result = collection.insert(objects.toArray, WriteConcern.Safe)
      log.info("Insert result: %s", result)
    }
  }

  def close = {
    connOpt foreach { conn =>
      log.info("Closing connection %s", conn)
      conn.close
    }
  }
}