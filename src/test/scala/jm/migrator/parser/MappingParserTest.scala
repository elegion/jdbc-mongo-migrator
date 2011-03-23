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
      result.head.mapping.fields("username") must equal (SimpleValue("u.username"))
      result.head.mapping.fields("counters.rating") must equal (ToInt("p.rating"))
      result.head.mapping.fields("counters.following") must equal (Count("following_following", "user_id = ${oldId}"))
      result.head.mapping.fields("counters.followers") must equal (Count("following_following", "victim_id = ${oldId}"))
      result.head.mapping.fields("groups") must equal ( Array (
        "groups AS g LEFT JOIN members AS m ON g.id = m.group_id",
        SimpleValue("LOWER(g.slug)"),
        "banned = 0 AND m.user_id = ${oldId}"))
      result.head.mapping.fields("groupPostCount") must equal ( CountMap (
        "posts AS p LEFT JOIN groups AS g ON p.group_id = g.id",
        "g.slug IS NOT NULL AND p.user_id = ${oldId}",
        "LOWER(g.slug)"
      ))
      result.head.mapping.fields("invites") must equal ( Array (
        "group_invites AS i LEFT JOIN groups AS g ON i.group_id = g.id",
        Fields(Map("group" -> SimpleValue("LOWER(g.slug)"), "invitedBy" -> StringMongoId("i.invited_by_id", "users"))),
        "i.user_id = ${oldId}"
      ))


    }

  }

}