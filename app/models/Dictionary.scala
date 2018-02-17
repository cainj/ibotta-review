package models

import javax.inject.Inject

import play.api.Configuration

import scala.io.Source

/**
 * Handles the dictionary words read from the configuration file.
 *
 * @param config The application configuration
 */
class Dictionary @Inject() (config: Configuration) {

  private[this] val dictionary = new scala.collection.mutable.HashMap[Int, String]()

  load(Source.fromInputStream(getClass.getResourceAsStream(config.get[String]("dictionary.file"))).getLines())

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
      dictionary.get(word.##).isDefined
  }

  /**
   * Loads the words into a map by the hashcode.
   * @param words The iterator of words
   */
  private[this] def load(words: Iterator[String]): Unit = words.foreach { word => dictionary += (word.## -> word) }

}
