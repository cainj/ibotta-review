package models

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class DictionarySpec extends PlaySpec with GuiceOneAppPerSuite {

  "The Dictionary Spec" should {
    "check validity of a word" in {
      val dictionary = new Dictionary(app.configuration)
      dictionary.valid("test") mustBe false
      dictionary.valid("aardvark") mustBe true
    }

    "check throw exception for empty string" in {
      val dictionary = new Dictionary(app.configuration)
      assertThrows[IllegalStateException] {
        dictionary.valid("")
      }
    }

  }
}
