package controllers

import javax.inject._

import akka.actor.ActorRef
import models._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc._
import utils.JsonUtils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  @Named("corpus-actor") corpus: ActorRef,
  cc: ControllerComponents) extends AbstractController(cc) {

  implicit val timeout: Timeout = 5.seconds

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action.async { implicit request =>
    Future {
      Ok(views.html.index())
    }
  }

  def check = Action.async(parse.tolerantJson) { request =>
    val anagrams = request.body.as[CreateAnagrams]
    (corpus ? CheckAnagrams(anagrams.words)).mapTo[Boolean] map {
      case true => Ok
      case false => BadRequest(Json.toJson(JsThrowable("Unable to store anagrams.", "Not valid anagrams.")))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def createAnagrams = Action.async(parse.tolerantJson) { request =>
    val anagrams = request.body.as[CreateAnagrams]
    (corpus ? CreateAnagrams(anagrams.words)).mapTo[Boolean] map {
      case true => Ok
      case false => InternalServerError(Json.toJson(JsThrowable("Unable to store anagrams", "Cause is unknown.")))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def anagrams(word: String, limit: Option[Int]) = Action.async {
    (corpus ? GetAnagrams(word, limit)).mapTo[Set[String]] map { anagrams =>
      Ok(Json.toJson(Anagrams(anagrams)))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def deleteWord(word: String) = Action.async {
    (corpus ? Delete(word)).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable(s"Unable to delete $word", "Cause is unknown.")))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def deleteAnagrams(word: String) = Action.async {
    (corpus ? DeleteAnagrams(word)).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable(s"Unable to delete $word", "Cause is unknown.")))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def delete = Action.async {
    (corpus ? Reset).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable("Unable to delete anagrams", "Cause is unknown.")))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def maxAnagrams = Action.async {
    (corpus ? MaxAnagrams).mapTo[List[Anagrams]] map { anagrams =>
      Ok(Json.toJson(anagrams))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def filter(size: Int) = Action.async {
    (corpus ? FilterBySize(size)).mapTo[List[Anagrams]] map { anagrams =>
      Ok(Json.toJson(anagrams))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  def stats = Action.async {
    (corpus ? GetStats).mapTo[Stats] map { stats =>
      Ok(Json.toJson(stats))
    } recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

}
