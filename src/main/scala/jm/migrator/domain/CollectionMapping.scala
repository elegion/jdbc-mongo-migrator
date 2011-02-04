package jm.migrator.domain

import scala.collection.mutable.StringBuilder

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:28 PM
 */

case class CollectionMapping(
  name: String,
  override val from: String,
  override val mapping: Fields) extends Select {

  def toSQL = {
    //val
    val builder = new StringBuilder("SELECT ")
    builder ++= (for {(field, MappedColumn(column)) <- mapping.fields}
      yield column ).mkString(", ")
    builder ++= " FROM " ++ from ++ " LIMIT 10"
    builder toString
  }
}
