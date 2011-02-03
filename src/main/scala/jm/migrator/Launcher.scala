package jm.migrator

import parser.MappingParser

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:13 PM
 */

object Launcher {
  val parser = new MappingParser()
  def main(args: Array[String]) = {
    val filename = args.headOption.getOrElse("./data/mapping.json")
    println (parser.parseFile(filename))

  }
}