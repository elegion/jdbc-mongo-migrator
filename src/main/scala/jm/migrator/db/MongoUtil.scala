package jm.migrator.db

import com.mongodb.casbah.Imports._


/**
 * Authod: Yuri Buyanov
 * Date: 2/4/11 2:36 PM
 */

object MongoUtil {
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

}