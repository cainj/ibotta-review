package models

import play.api.libs.json.{ Json, OWrites }

case class Stats(min: Int, max: Int, median: Int, average: Double)

object Stats {

  def empty = Stats(0, 0, 0, 0.0)

  implicit val writes: OWrites[Stats] = Json.writes[Stats]

}