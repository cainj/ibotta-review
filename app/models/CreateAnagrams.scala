package models

import play.api.libs.json.{ Json, Reads }

case class CreateAnagrams(words: Set[String])

object CreateAnagrams {

  implicit val reads: Reads[CreateAnagrams] = Json.reads[CreateAnagrams]

}
