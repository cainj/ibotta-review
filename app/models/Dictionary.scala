package models

import javax.inject.Inject

import play.api.Configuration

import scala.collection.immutable.HashMap
import scala.io.Source

/**
 * Handles the dictionary words read from the configuration file.
 *
 * @param config The application configuration
 */
class Dictionary @Inject() (config: Configuration) {

  private[this] val words = Source.fromInputStream(getClass.getResourceAsStream(config.get[String]("dictionary.file"))).getLines()
  private[this] val dictionary = words.foldLeft(HashMap.empty[String, String]) { (accum, next) =>
    accum + (next -> next)
  }

  /**
   * Checks to see if the word valid.
   *
   * @param word The word to check.
   * @return
   */
  def valid(word: String): Boolean = {
    if (word.isEmpty)
      throw new IllegalStateException("Empty string.")
    else
      dictionary.get(word).isDefined
  }

}
