package jm.migrator.domain

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:10 PM
 */

abstract sealed class MappedValue

/**
 * Maps to primitive bson value
 */
class ObjectValue(val column: String) extends MappedValue

/**
 * Maps to DBObject with fields
 */
class Fields(val fields: Map[String, MappedValue]) extends MappedValue

/**
 * Maps subselect results to embedded array
 */
class Array(
  override val from: String,
  override val mapping: Fields,
  override val where: String = ""
) extends MappedValue with Select

