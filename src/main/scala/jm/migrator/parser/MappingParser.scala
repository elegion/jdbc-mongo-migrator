package jm.migrator.parser

import net.liftweb.json.JsonParser._
import io.Source
import java.io.{FileInputStream, File, InputStream}


/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 4:53 PM
 */

class MappingParser {
  def parseFile(filename: String) = {
    println("filename: "+filename)
    val input = Source.fromFile(filename).mkString
    println("INPUT: "+input)
    val json = parse(input)
  }




}