package jm.migrator.domain

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:10 PM
 */

abstract sealed class MappedValue

/**
 * Indicates that value is mapped to a single column
 */
trait MappedColumn {
  def column: String
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
case class MongoId(column: String) extends MappedValue with MappedColumn

/**
 * Maps to DBObject with fields
 */
case class Fields(fields: Map[String, MappedValue]) extends MappedValue

/**
 * Maps subselect results to embedded array
 */
case class Array(
  override val from: String,
  override val mapping: MappedValue,
  override val where: String = ""
) extends MappedValue with Select

