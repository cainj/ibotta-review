package models

import akka.actor.Actor

import scala.util.Try

/**
 * Thread safe actor the analyzes the data store for metric related data.
 *
 */
class Analyzer extends Actor {
  private[this] var average: Double = 0
  private[this] var min: Int = 0
  private[this] var max: Int = 0
  private[this] var median: Int = 0
  private[this] var words: Iterable[Set[String]] = Iterable.empty

  override def receive = {
    case Analyze(corpus) => analyze(corpus)
    case GetStats => sender ! Stats(min, max, median, average)
    case MaxAnagrams => sender ! words.toList.map { Anagrams(_) }
  }

  private[this] def analyze(corpus: Map[String, Set[String]]) {
    val anagrams = corpus.values
    val stats = anagrams.flatten.map { _.length }.asInstanceOf[Seq[Int]].sorted
    max = Try { stats.max } getOrElse 0
    min = Try { stats.min } getOrElse 0
    average = Try { stats.sum.toDouble / stats.size.toDouble } map { n => if (n.isNaN) 0.0 else n } getOrElse 0.0
    median = Try { stats(stats.size / 2) } getOrElse 0
    val maxWordLength = Try { anagrams.maxBy(_.size).size } getOrElse 0
    words = anagrams.filter(_.size == maxWordLength)
  }
}
