package jm.migrator.domain

import jm.migrator.db.MongoUtil

import jm.migrator.util.Implicits._
import jm.migrator.util.ShortUrlEncoder

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:10 PM
 */

abstract sealed class MappedValue


/**
 * Value mapped to columns expression
 */
trait Selectable extends MappedValue {
  def columnsString: String
}


/**
 * Indicates that value is mapped to a single column
 */
trait MappedColumn extends Selectable {
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
case class SimpleValue(column: String) extends MappedColumn

/**
 * Generates object ID and saves binds it to column value for future references
 * @returns it as string
 */
case class ToInt(column: String) extends MappedColumn {
  override def toValue(sqlValue: Any) = Option(sqlValue) map ( v => v.toString.toInt) orNull
}

/**
 * Generates object ID and saves binds it to column value for future references
 * @returns it as string
 */
case class ToLong(column: String) extends MappedColumn {
  override def toValue(sqlValue: Any) = Option(sqlValue) map ( v => v.toString.toLong) orNull
}


/**
 * Generates object ID and saves binds it to column value for future references
 */
case class MongoId(column: String, collection: String) extends MappedColumn {
  override def toValue(sqlValue: Any) = MongoUtil.getMongoId(sqlValue, collection)

}

/**
 * Generates object ID and saves binds it to column value for future references
 * @returns it as string
 */
case class StringMongoId(column: String, collection: String) extends MappedColumn {
  override def toValue(sqlValue: Any) = MongoUtil.getMongoId(sqlValue, collection).toString
}

/**
 * Generates short url
 * @returns it as string
 */
case class ShortUrl(expression: String) extends MappedValue with MappedColumn {
  override def toValue(sqlValue: Any) = {
    val n = sqlValue.asInstanceOf[Int]
    expression.render(Map(column -> ShortUrlEncoder.encodeUrl(n)))
  }


  def column = expression.placeholders.head
}

/**
 * Maps to DBObject with fields
 */
case class Fields(fields: Map[String, MappedValue]) extends Selectable {
  def columnsString = (for {(field, MappedColumn(column)) <- fields}
    yield column ).mkString(", ")
}

/**
 * Maps subselect results to embedded array
 */
case class Array(
  override val from: String,
  override val mapping: Selectable,
  override val where: String = ""
) extends MappedValue with Select

/**
 * Maps several column values to array
 */
case class ColArray(cols: Seq[String]) extends Selectable {
  def columnsString = cols.mkString(", ")
}


/**
 * Maps subselect results to embedded array
 */
case class Count(
  override val from: String,
  override val where: String = ""
) extends MappedValue with Select {
  def mapping = SimpleValue("COUNT(*)")
}

/**
 * Maps subselect results to embedded array
 */
case class CountMap(
  override val from: String,
  override val where: String = "",
  key: String
) extends MappedValue with Select {
  override def toSQL(expressionParams: Map[String, Any]) = super.toSQL(expressionParams) + " GROUP BY "+key
  def mapping = Fields(Map("key" -> SimpleValue(key), "value" -> SimpleValue("COUNT(*)")))
}


