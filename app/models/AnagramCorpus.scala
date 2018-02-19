package models

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import language.postfixOps
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Thread safe actor that manages the data store for the anagrams.
 *
 * @param dictionary The english dictionary
 */
@Singleton
class AnagramCorpus @Inject() (dictionary: Dictionary) extends Actor {

  private type Anagrams = Set[String]
  private[this] var store = Map.empty[String, Anagrams]
  private[this] val analyzer = context.actorOf(Props(new Analyzer))

  implicit val timeout: Timeout = 5.seconds

  override def receive = {
    case CreateAnagrams(words) => store(words) pipeTo sender
    case Reset => reset pipeTo sender
    case GetStats => (analyzer ? GetStats) pipeTo sender
    case Delete(word) => deleteWord(word) pipeTo sender
    case MaxAnagrams => (analyzer ? MaxAnagrams) pipeTo sender
    case GetAnagrams(word, limit) => get(word, limit) pipeTo sender
    case DeleteAnagrams(word) => deleteAnagrams(word) pipeTo sender
    case FilterBySize(size) => filter(size) pipeTo sender
    case Corpus => sender ! store.values
    case CheckAnagrams(words: Set[String]) =>
      val lowerCase = words.map(_.toLowerCase)
      sender ! isAnagrams(lowerCase, lowerCase.head.sorted)
  }

  def filter(size: Int) = {
    val promise: Promise[Iterable[models.Anagrams]] = Promise.apply()
    val f = promise.future
    val anagrams = store.values.filter(_.size >= size) map { Anagrams(_) }
    promise success anagrams
    f
  }

  def deleteWord(word: String): Future[Boolean] = {
    val promise: Promise[Boolean] = Promise.apply()
    val f = promise.future
    val anagrams = findAnagrams(word, delete = true)
    if (anagrams.nonEmpty) store = store + (key(word) -> anagrams)
    else store = store - key(word)
    analyzer ! Analyze(store)
    promise success !anagrams.contains(word)
    f
  }

  def deleteAnagrams(word: String): Future[Boolean] = {
    val promise: Promise[Boolean] = Promise.apply()
    val f = promise.future
    val k = key(word)
    store = store - k
    analyzer ! Analyze(store)
    promise success !store.isDefinedAt(k)
    f
  }

  /**
    * Gets the anagrams
    *
    * @param word The word.
    * @param limit The number of anagrams to get.
    * @return
    */
  def get(word: String, limit: Option[Int] = None): Future[Anagrams] = {
    val promise: Promise[Anagrams] = Promise.apply()
    val f = promise.future
    val anagrams = findAnagrams(word)
    promise success (anagrams take limit.getOrElse(anagrams.size))
    f
  }

  def reset: Future[Boolean] = {
    val promise: Promise[Boolean] = Promise.apply()
    val f = promise.future
    store = Map.empty[String, Anagrams]
    analyzer ! Analyze(store)
    promise success store.isEmpty
    f
  }

  /**
    * Stores the words in the corpus
    *
    * @param words The list of anagrams
    * @return
    */
  def store(words: Anagrams): Future[Boolean] = {
    val promise: Promise[Boolean] = Promise.apply()
    val f = promise.future

    val lowerCase = words.map(_.toLowerCase)
    //validate that they are english words

    if (lowerCase.forall(dictionary.valid)) {
      //validate that they are anagrams
      val key = lowerCase.head.sorted
      if (isAnagrams(lowerCase, key)) {
        store = store + (key -> words)
        analyzer ! Analyze(store)
        promise success true
      } else sender() ! akka.actor.Status.Failure(new IllegalStateException("Doesn't satisfy anagrams.  https://en.wikipedia.org/wiki/Anagram"))
    } else sender() ! akka.actor.Status.Failure(new IllegalArgumentException("Invalid word found in the list."))
    f
  }
  
  private[this] def key(word: String) = word.toLowerCase.sorted

  private def isAnagrams(test: Set[String], key: String) = test forall (_.sorted == key)

  private[this] def findAnagrams(word: String, delete: Boolean = false): Anagrams = {
    val anagrams = store.getOrElse(key(word), Set.empty)
    if (anagrams.contains(word)) anagrams filterNot (word==) else if (delete) anagrams else Set.empty
  }

}

object AnagramCorpus {
  def props(dictionary: Dictionary): Props = Props(new AnagramCorpus(dictionary))
}