package jm.migrator.parser;

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers

import jm.migrator.domain._

/*
 * Author: Yuri Buyanov
 * Date: 3/17/11 12:38 PM
 */
class MappingParserTest extends Spec with MustMatchers {


  val parser = new MappingParser
  val url = getClass.getResource("/test_mapping.json").getFile

  describe("MappingParser should parse test mapping "+url+" and return CollectionMapping iterator") {
    val result = parser.parseFile(url)

    it ("should contain one collection mapping named 'users'") {
      result must have size (1)
      result.head.name must be ("users")
      result.head.mapping.fields must have size (10)
      result.head.mapping.fields("_id") must equal (MongoId("u.id", "users"))
      result.head.mapping.fields("stringId") must equal (StringMongoId("u.id", "users"))
      result.head.mapping.fields("oldId") must equal (SimpleValue("u.id"))
    }

  }

}