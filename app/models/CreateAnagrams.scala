package models

import play.api.libs.json.Json

case class CreateAnagrams(words: Set[String])

object CreateAnagrams {

  implicit val reads = Json.reads[CreateAnagrams]

}
