package controllers

import models.{ Anagrams, Stats }
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ApiSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val appWithRoutes: Application = GuiceApplicationBuilder().overrides().build()

  "Api Tests " must {
    "pass" in new WithServer(app = appWithRoutes, port = 3333) {

      val baseUrl = s"http://localhost:3333"
      val ws = app.injector.instanceOf(classOf[WSClient])
      var json = Json.parse("""{
                              |  "words": [
                              |  	  "eat",
                              |    "ate",
                              |    "tea"
                              |  ]
                              |}""".stripMargin)

      var createAnagrams = Await.result(ws.url(s"$baseUrl/words.json").post(json), Duration(5, "seconds"))
      createAnagrams.status mustBe OK

      json = Json.parse("""
          |{
          |  "words": [
          |  	 "eat",
          |    "at",
          |    "tea"
          |  ]
          |}
        """.stripMargin)

      val createAnagramsError = Await.result(ws.url(s"$baseUrl/words.json").post(json), Duration(5, "seconds"))
      createAnagramsError.status mustBe BAD_REQUEST

      json = Json.parse("""{
                              |  "words": [
                              |  	  "eat",
                              |    "ate",
                              |    "tea"
                              |  ]
                              |}""".stripMargin)

      val checkAnagram = Await.result(ws.url(s"$baseUrl/anagrams/check").post(json), Duration(5, "seconds"))
      checkAnagram.status mustBe OK

      json = Json.parse("""{
                          |  "words": [
                          |  	  "eat",
                          |    "at",
                          |    "tea"
                          |  ]
                          |}""".stripMargin)

      val checkAnagramError = Await.result(ws.url(s"$baseUrl/anagrams/check").post(json), Duration(5, "seconds"))
      checkAnagramError.status mustBe BAD_REQUEST

      val stats = Await.result(ws.url(s"$baseUrl/anagrams/stats").get(), Duration(5, "seconds"))
      stats.status mustBe OK
      stats.json.as[Stats] mustBe Stats(3, 3, 3, 3)

      var filterResponse = Await.result(ws.url(s"$baseUrl/anagrams/filter/4").get(), Duration(5, "seconds"))
      filterResponse.status mustBe OK
      filterResponse.json.as[List[Anagrams]] mustBe List.empty

      filterResponse = Await.result(ws.url(s"$baseUrl/anagrams/filter/2").get(), Duration(5, "seconds"))
      filterResponse.status mustBe OK
      filterResponse.json.as[List[Anagrams]] mustBe List(Anagrams(Set("eat", "ate", "tea")))

      var anagramsResponse = Await.result(ws.url(s"$baseUrl/anagrams/ate.json?limit=1").get(), Duration(5, "seconds"))
      anagramsResponse.status mustBe OK
      anagramsResponse.json.as[Anagrams] mustBe Anagrams(Set("eat"))

      val deleteWord = Await.result(ws.url(s"$baseUrl/words/eat.json").delete(), Duration(5, "seconds"))
      deleteWord.status mustBe NO_CONTENT

      anagramsResponse = Await.result(ws.url(s"$baseUrl/anagrams/ate.json").get(), Duration(5, "seconds"))
      anagramsResponse.status mustBe OK
      anagramsResponse.json.as[Anagrams] mustBe Anagrams(Set("tea"))

      val deleteWordAnagrams = Await.result(ws.url(s"$baseUrl/anagrams/ate.json").delete(), Duration(5, "seconds"))
      deleteWordAnagrams.status mustBe NO_CONTENT

      anagramsResponse = Await.result(ws.url(s"$baseUrl/anagrams/tea.json").get(), Duration(5, "seconds"))
      anagramsResponse.status mustBe OK

      json = Json.parse("""{
                          |  "words": [
                          |  	 "eat",
                          |    "ate",
                          |    "tea"
                          |  ]
                          |}""".stripMargin)

      createAnagrams = Await.result(ws.url(s"$baseUrl/words.json").post(json), Duration(5, "seconds"))
      createAnagrams.status mustBe OK

      json = Json.parse("""{
                          |  "words": [
                          |  	 "bear",
                          |    "bare"
                          |  ]
                          |}""".stripMargin)

      createAnagrams = Await.result(ws.url(s"$baseUrl/words.json").post(json), Duration(5, "seconds"))
      createAnagrams.status mustBe OK

      val maxAnagrams = Await.result(ws.url(s"$baseUrl/anagrams/max").get(), Duration(5, "seconds"))
      maxAnagrams.status mustBe OK
      maxAnagrams.json.as[List[Anagrams]] mustBe List(Anagrams(Set("eat", "ate", "tea")))

      val reset = Await.result(ws.url(s"$baseUrl/words.json").delete(), Duration(5, "seconds"))
      reset.status mustBe NO_CONTENT

      val entireCorpus = Await.result(ws.url(s"$baseUrl/corpus").get(), Duration(5, "seconds"))
      entireCorpus.status mustBe OK
      entireCorpus.json.as[List[Anagrams]] mustBe List.empty
    }
  }
}

