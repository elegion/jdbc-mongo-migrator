package jm.migrator.domain

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 1:29 PM
 */

trait Select {
  def mapping: MappedValue
  def from: String
  def where: String = ""
}