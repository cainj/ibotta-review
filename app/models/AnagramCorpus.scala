package models

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import language.postfixOps
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
    case CreateAnagrams(words) => sender ! store(words)
    case Reset => sender ! reset
    case GetStats => (analyzer ? GetStats) pipeTo sender
    case Delete(word) => sender ! deleteWord(word)
    case MaxAnagrams => (analyzer ? MaxAnagrams) pipeTo sender
    case GetAnagrams(word, limit) => sender ! get(word, limit)
    case DeleteAnagrams(word) => sender ! deleteAnagrams(word)
    case FilterBySize(size) => sender ! filter(size)
    case Corpus => sender ! store.values
    case CheckAnagrams(words: Set[String]) =>
      val lowerCase = words.map(_.toLowerCase)
      sender ! isAnagrams(lowerCase, lowerCase.head.sorted)
  }

  def filter(size: Int) = store.values.filter(_.size >= size) map { Anagrams(_) }

  def deleteWord(word: String): Boolean = {
    val anagrams = findAnagrams(word, delete = true)
    if (anagrams.nonEmpty) store = store + (key(word) -> anagrams)
    else store = store - key(word)
    analyzer ! Analyze(store)
    !anagrams.contains(word)
  }

  def deleteAnagrams(word: String): Boolean = {
    val k = key(word)
    store = store - k
    analyzer ! Analyze(store)
    !store.isDefinedAt(k)
  }

  def get(word: String, limit: Option[Int] = None): Anagrams = {
    val anagrams = findAnagrams(word)
    anagrams take limit.getOrElse(anagrams.size)
  }

  def reset: Boolean = {
    store = Map.empty[String, Anagrams]
    analyzer ! Analyze(store)
    store.isEmpty
  }

  def store(words: Anagrams): Boolean = {
    var stored = false
    //validate that they are english words
    if (words.forall(dictionary.valid)) {
      val lowerCase = words.map(_.toLowerCase)
      //validate that they are anagrams
      val key = lowerCase.head.sorted
      if (isAnagrams(lowerCase, key)) {
        store = store + (key -> words)
        analyzer ! Analyze(store)
        stored = true
      } else sender() ! akka.actor.Status.Failure(new IllegalStateException("Doesn't satisfy anagrams.  https://en.wikipedia.org/wiki/Anagram"))
    } else sender() ! akka.actor.Status.Failure(new IllegalArgumentException("Invalid word found in the list."))
    stored
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