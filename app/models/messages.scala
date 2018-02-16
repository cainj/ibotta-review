package models

case object Reset

case class Analyze(map: Map[String, Set[String]])

case object GetStats

case object MaxAnagrams

case class Delete(word: String)

case class DeleteAnagrams(word: String)

case class GetAnagrams(word: String, limit: Option[Int] = None)

case class CheckAnagrams(word: Set[String])

case class FilterBySize(size: Int)
