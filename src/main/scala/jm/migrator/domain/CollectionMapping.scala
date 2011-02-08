package jm.migrator.domain

import scala.collection.mutable.StringBuilder

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:28 PM
 */

case class CollectionMapping(
  name: String,
  override val from: String,
  override val mapping: Fields,
  override val where: String = "") extends Select {
}
