package jm.migrator.parser

import net.liftweb.json.JsonParser._
import io.Source
import java.io.{FileInputStream, File, InputStream}
import net.liftweb.json._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.DefaultFormats

import jm.migrator.domain._



/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 4:53 PM
 */

class MappingParser {
  def parseFile(filename: String) = {
    println("Parsing filename: "+filename)
    val input = Source.fromFile(filename).mkString
    val json = parse(input)
    val children = json \ "collections" children

    val collections = for {
      JObject(list) <- children
      JField(name, data) <- list
    } yield parseCollection(name, data)

    implicit val formats = DefaultFormats
    println ("COLLECTIONS: " + collections)
  }

  def parseCollection(name: String, json: JValue): CollectionMapping = {
    val from = (json \ "from" \ classOf[JString])(0)
    val jfields = Map(json \ "mapping" \ classOf[JField]: _*)
    val fields = jfields mapValues getMapping
    CollectionMapping(name, from, Fields(fields))
  }

  def getMapping(obj: Any): MappedValue = {
    obj match {
      case column: String =>
        SimpleValue(column)
      case m: Map[String, String] if m.contains("$oid") =>
        MongoId(m.get("$oid").get)
      case m: Map[String, Any] =>
        parseSubselect(m)
      case unknown => throw new Exception("Unknown field type: "+unknown)
    }
  }

  def parseSubselect(subselect: Map[String, Any]): Array = {
    val from = subselect.getOrElse("from", throw new Exception("No 'from' specified" + subselect)).toString
    val mapping = subselect.get("mapping") map getMapping getOrElse (throw new Exception("No 'mapping' specified" + subselect))
    val where = subselect.get("where").getOrElse("").toString
    Array(from, mapping, where)
  }




}