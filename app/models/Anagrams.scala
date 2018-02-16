package models

import play.api.libs.json.{ Json, OWrites }

case class Anagrams(anagrams: Set[String])

object Anagrams {

  implicit val writes: OWrites[Anagrams] = Json.writes[Anagrams]

}