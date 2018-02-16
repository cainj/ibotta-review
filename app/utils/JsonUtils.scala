package utils

import models.JsThrowable
import play.api.libs.json._

import scala.language.implicitConversions

object JsonUtils {

  implicit def throwableJsonWrapper(throwable: Throwable): JsValue = {
    import models.JsThrowable.writes
    Json.toJson(JsThrowable(throwable.getClass.getName, throwable.getMessage))
  }
}
