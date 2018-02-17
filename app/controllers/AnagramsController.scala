package controllers

import javax.inject._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc._

import models._
import utils.JsonUtils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class AnagramsController @Inject() (
  @Named("corpus-actor") corpusActor: ActorRef,
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

  def anagrams(word: String, limit: Option[Int]) = asyncRecover { implicit request =>
    (corpusActor ? GetAnagrams(word, limit)).mapTo[Set[String]] map { anagrams =>
      Ok(Json.toJson(Anagrams(anagrams)))
    }
  }

  def check = Action.async(parse.tolerantJson) { request =>
    asyncWithRecover {
      val anagrams = request.body.as[CreateAnagrams]
      (corpusActor ? CheckAnagrams(anagrams.words)).mapTo[Boolean] map {
        case true => Ok
        case false => BadRequest(Json.toJson(JsThrowable("Unable to store anagrams.", "Not valid anagrams.")))
      }
    }
  }

  def createAnagrams(allowProper: Option[Boolean]) = Action.async(parse.tolerantJson) { request =>
    asyncWithRecover {
      val anagrams = request.body.as[CreateAnagrams]
      if (!allowProper.getOrElse(true) && anagrams.words.exists(isCapital)) Future.successful(NotAcceptable)
      else {
        (corpusActor ? CreateAnagrams(anagrams.words)).mapTo[Boolean] map {
          case true => Ok
          case false => InternalServerError(Json.toJson(JsThrowable("Unable to store anagrams", "Cause is unknown.")))
        }
      }
    }
  }

  def corpus = asyncRecover { implicit request =>
    (corpusActor ? Corpus).mapTo[Iterable[List[String]]] map { anagrams =>
      Ok(Json.toJson(anagrams))
    }
  }

  def deleteWord(word: String) = asyncRecover { implicit request =>
    (corpusActor ? Delete(word)).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable(s"Unable to delete $word", "Cause is unknown.")))
    }
  }

  def deleteAnagrams(word: String) = asyncRecover { implicit request =>
    (corpusActor ? DeleteAnagrams(word)).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable(s"Unable to delete $word", "Cause is unknown.")))
    }
  }

  def delete = asyncRecover { implicit request =>
    (corpusActor ? Reset).mapTo[Boolean] map {
      case true => NoContent
      case false => InternalServerError(Json.toJson(JsThrowable("Unable to delete anagrams", "Cause is unknown.")))
    }
  }

  def filter(size: Int) = asyncRecover { implicit request =>
    (corpusActor ? FilterBySize(size)).mapTo[List[Anagrams]] map { anagrams =>
      Ok(Json.toJson(anagrams))
    }
  }

  def maxAnagrams = asyncRecover { implicit request =>
    (corpusActor ? MaxAnagrams).mapTo[List[Anagrams]] map { anagrams =>
      Ok(Json.toJson(anagrams))
    }
  }

  def stats = asyncRecover { implicit request =>
    (corpusActor ? GetStats).mapTo[Stats] map { stats =>
      Ok(Json.toJson(stats))
    }
  }

  def asyncRecover(block: Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      asyncWithRecover {
        block(request)
      }
    }
  }

  private[this] def asyncWithRecover(f: => Future[Result]) = {
    f recover {
      case t: Throwable => BadRequest(throwableJsonWrapper(t))
    }
  }

  private[this] def isCapital(word: String) = word.headOption exists { c => c.isUpper }
}
