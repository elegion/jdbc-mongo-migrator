package jm.migrator.domain

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:28 PM
 */

class Collection(
  val name: String,
  override val from: String,
  override val mapping: Fields) extends Select
