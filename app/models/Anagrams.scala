package models

import play.api.libs.json.{ Json, OWrites, Reads }

case class Anagrams(anagrams: Set[String])

object Anagrams {

  implicit val writes: OWrites[Anagrams] = Json.writes[Anagrams]

  implicit val reads: Reads[Anagrams] = Json.reads[Anagrams]

}