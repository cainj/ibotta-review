package models

import play.api.libs.json._

import scala.language.implicitConversions

case class JsThrowable(error: String, details: String)

object JsThrowable {
  implicit val writes: Writes[JsThrowable] = Json.writes[JsThrowable]
}

