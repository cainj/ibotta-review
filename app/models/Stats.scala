package models

import play.api.libs.json.{ Json, Reads, Writes }

case class Stats(min: Int, max: Int, median: Int, average: Double)

object Stats {
  def empty = Stats(0, 0, 0, 0.0)

  implicit val writes: Writes[Stats] = Json.writes[Stats]

  implicit val reads: Reads[Stats] = Json.reads[Stats]

}