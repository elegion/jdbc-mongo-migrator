package jm.migrator.util

import jm.migrator.domain.Expression

/**
 * Authod: Yuri Buyanov
 * Date: 2/3/11 2:39 PM
 */

object Implicits {
  implicit def string2Expression(s: String) = Expression(s)

}