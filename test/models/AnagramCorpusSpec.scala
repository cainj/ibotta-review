package models

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AnagramCorpusSpec extends PlaySpec with GuiceOneAppPerSuite {

  private[this] val dictionary = new Dictionary(app.configuration)

  "The AnagramCorpus Spec" should {

    "Return true if they are valid anagrams" in {
      val corpus = new AnagramCorpus(dictionary = dictionary)
      Await.result(corpus.store(Set("read", "dare", "dear")), Duration(5, "seconds")) mustBe true
      //ignores case
      Await.result(corpus.store(Set("teaL", "tAle")), Duration(5, "seconds")) mustBe true
    }

    "Throw an exception if word not found in dictionary" in {
      val corpus = new AnagramCorpus(dictionary = dictionary)
      assertThrows[IllegalArgumentException] {
        corpus.store(Set("read", "eard", "dear"))
      }
    }

    "Throw an exception if the anagram definition is not satisfied" in {
      val corpus = new AnagramCorpus(dictionary = dictionary)
      assertThrows[IllegalStateException] {
        corpus.store(Set("read", "red", "dear"))
      }
    }

    "Return list of anagrams if they are valid anagrams" in {
      val corpus = new AnagramCorpus(dictionary = dictionary)
      corpus.store(Set("read", "dare", "dear"))
      corpus.store(Set("teal", "tale"))
      Await.result(corpus.get("dare"), Duration(5, "seconds")) mustBe List("read", "dear")
      Await.result(corpus.get("teal"), Duration(5, "seconds")) mustBe List("tale")
      Await.result(corpus.get("dare", Some(4)), Duration(5, "seconds")) mustBe List("read", "dear")
      Await.result(corpus.get("dare", Some(1)), Duration(5, "seconds")) mustBe List("read")
    }
  }

}
