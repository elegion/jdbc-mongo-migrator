package jm.migrator.domain

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:29 PM
 */

trait Select {
  def mapping: MappedValue
  def from: String
  def where: String = ""

  def toSQL = {
    val builder = new StringBuilder("SELECT ")
    builder ++= mapping.columnsString
    builder ++= " FROM " ++ from ++ " LIMIT 10"
    builder toString
  }
}