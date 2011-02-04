package jm.migrator.migration

import jm.migrator.domain.CollectionMapping

import jm.migrator.db.DBUtil._

/**
 * Authod: Yuri Buyanov
 * Date: 2/4/11 11:52 AM
 */

class SQLImporter(val mapping: Iterable[CollectionMapping] ) {

  def fetch = {
    using(connection) { conn =>
      mapping map ( _.toSQL )
    }
  }
}