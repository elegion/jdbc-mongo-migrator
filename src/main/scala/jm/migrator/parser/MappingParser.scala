package jm.migrator.parser

import net.liftweb.json.JsonParser._
import io.Source
import java.io.{FileInputStream, File, InputStream}
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.DefaultFormats

import jm.migrator.domain._

import net.lag.logging.Logger



/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 4:53 PM
 */

class MappingParser {
  val log = Logger.get


  def parseFile(filename: String): Iterable[CollectionMapping] = {
    println("Parsing filename: "+filename)
    val input = Source.fromFile(filename).mkString
    val json = parse(input)
    val children = json \ "collections" children

    for {
      JObject(list) <- children
      JField(name, data) <- list
    } yield parseCollection(name, data)
  }

  def parseCollection(name: String, json: JValue): CollectionMapping = {
    val from = (json \ "from" \ classOf[JString])(0)
    val jfields = Map(json \ "mapping" \ classOf[JField]: _*)
    val where = (json \ "where" \ classOf[JString]).headOption.getOrElse("")
    val fields = jfields mapValues getMapping(name)
    CollectionMapping(name, from, Fields(fields), where)
  }

  def getMapping(collection: String)(obj: Any): MappedValue = {
    obj match {
      case column: String =>
        SimpleValue(column)
      case m: Map[String, Any] if m.contains("$surl") =>
        m.get("$surl") match {
          case Some(expr: String) => ShortUrl(expr)
          case unknown  => throw new Exception("Incorrect $oid mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$int") =>
        m.get("$int") match {
          case Some(key: String) => ToInt(key)
          case unknown  => throw new Exception("Incorrect $int mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$long") =>
        m.get("$long") match {
          case Some(key: String) => ToLong(key)
          case unknown  => throw new Exception("Incorrect $long mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$oid") =>
        m.get("$oid") match {
          case Some(key: String) => MongoId(key, collection)
          case Some(oidData: Map[String, String]) => MongoId(oidData.get("key").get, oidData.get("collection").get)
          case unknown  => throw new Exception("Incorrect $oid mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$oidString") =>
        m.get("$oidString") match {
          case Some(key: String) => StringMongoId(key, collection)
          case Some(oidData: Map[String, String]) => StringMongoId(oidData.get("key").get, oidData.get("collection").get)
          case unknown  => throw new Exception("Incorrect $oidString mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$array") =>
        m.get("$array") match {
          case Some(array: Map[String, Any]) => parseSubselect(array, collection)
          case unknown  => throw new Exception("Incorrect $array mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$colArray") =>
        m.get("$colArray") match {
          case Some(array: Seq[String]) => ColArray(array)
          case unknown  => throw new Exception("Incorrect $colArray mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$count") =>
        m.get("$count") match {
          case Some(count: Map[String, Any]) => parseCount(count)
          case unknown  => throw new Exception("Incorrect $count mapping: "+unknown)
        }
      case m: Map[String, Any] if m.contains("$countMap") =>
        m.get("$countMap") match {
          case Some(countMap: Map[String, Any]) => parseCountMap(countMap)
          case unknown  => throw new Exception("Incorrect $count mapping: "+unknown)
        }
      case m: Map[String, Any] =>
        Fields(m mapValues getMapping(collection))
      case unknown => throw new Exception("Unknown field type: "+unknown)
    }
  }

  def parseCount(subselect: Map[String, Any]) = {
    val from = subselect.getOrElse("from", throw new Exception("No 'from' specified: " + subselect)).toString
    val where = subselect.get("where").getOrElse("").toString
    Count(from, where)
  }

  def parseCountMap(countMap: Map[String, Any]) = {
    val from = countMap.getOrElse("from", throw new Exception("No 'from' specified: " + countMap)).toString
    val where = countMap.get("where").getOrElse("").toString
    val key = countMap.get("key").getOrElse(throw new Exception("No 'key' specified: " + countMap)).toString
    CountMap(from, where, key)
  }


  def parseSubselect(subselect: Map[String, Any], collection: String): Array = {
    val from = subselect.getOrElse("from", throw new Exception("No 'from' specified: " + subselect)).toString
    val mapping = subselect get("mapping") map getMapping(collection) match {
      case Some(cs: Selectable) => cs
      case Some(mv) => throw new Exception("Invalid subselect mapping: " + mv)
      case None => throw new Exception("No 'mapping' specified" + subselect)
    }
    val where = subselect.get("where").getOrElse("").toString
    Array(from, mapping, where)
  }




}