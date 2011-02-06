package jm.migrator.domain

import jm.migrator.db.MongoUtil

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:10 PM
 */

abstract sealed class MappedValue {
  def columnsString: String
}

/**
 * Indicates that value is mapped to a single column
 */
trait MappedColumn extends MappedValue {
  def column: String
  def columnsString = column
  def toValue(sqlValue: Any): Any = sqlValue
}

object MappedColumn {
  def unapply(col: MappedColumn) = Some(col.column)
}

/**
 * Maps to primitive bson value
 */
case class SimpleValue(column: String) extends MappedValue with MappedColumn

/**
 * Generates object ID and saves binds it to column value for future references
 */
case class MongoId(column: String, collection: String) extends MappedValue with MappedColumn {
  override def toValue(sqlValue: Any) = MongoUtil.getMongoId(sqlValue, collection)

}

/**
 * Generates object ID and saves binds it to column value for future references
 * @returns it as string
 */
case class StringMongoId(column: String, collection: String) extends MappedValue with MappedColumn {
  override def toValue(sqlValue: Any) = MongoUtil.getMongoId(sqlValue, collection).toString

}


/**
 * Maps to DBObject with fields
 */
case class Fields(fields: Map[String, MappedValue]) extends MappedValue {
  def columnsString = (for {(field, MappedColumn(column)) <- fields}
    yield column ).mkString(", ")
}

/**
 * Maps subselect results to embedded array
 */
case class Array(
  override val from: String,
  override val mapping: MappedValue,
  override val where: String = ""
) extends MappedValue with Select {
  def columnsString = null //TODO -- refactor and remove
}

