package models

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestActors, TestKit }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

class AnagramCorpusSpec extends TestKit(ActorSystem("AnagramCorpusSpec")) with ImplicitSender with WordSpecLike
  with BeforeAndAfterAll with MustMatchers with GuiceOneAppPerSuite {

  implicit val timeout: Timeout = 5.seconds

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "The AnagramCorpus Spec" should {

    "send back messages unchanged" in {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }

    "Return true if they are valid anagrams" in {
      val corpus = app.actorSystem.actorOf(AnagramCorpus.props(new Dictionary(app.configuration)))
      Await.result((corpus ? CreateAnagrams(Set("read", "dare", "dear"))).mapTo[Boolean], Duration(5, "seconds")) mustBe true
      //ignores case
      assertThrows[IllegalArgumentException] {
        Await.result((corpus ? CreateAnagrams(Set("teaL", "tAle"))).mapTo[Boolean], Duration(5, "seconds")) mustBe true
      }
    }

    "Check to see if they are valid anagrams" in {
      val corpus = app.actorSystem.actorOf(AnagramCorpus.props(new Dictionary(app.configuration)))
      Await.result((corpus ? CheckAnagrams(Set("read", "dare", "dear"))).mapTo[Boolean], Duration(5, "seconds")) mustBe true
      Await.result((corpus ? CheckAnagrams(Set("teal", "let"))).mapTo[Boolean], Duration(5, "seconds")) mustBe false
    }

    "Throw an exception if word not found in dictionary" in {
      val corpus = app.actorSystem.actorOf(AnagramCorpus.props(new Dictionary(app.configuration)))
      assertThrows[IllegalArgumentException] {
        Await.result((corpus ? CreateAnagrams(Set("read", "eard", "dear"))).mapTo[Boolean], Duration(5, "seconds"))
      }
    }

    "Throw an exception if the anagram definition is not satisfied" in {
      val corpus = app.actorSystem.actorOf(AnagramCorpus.props(new Dictionary(app.configuration)))
      assertThrows[IllegalStateException] {
        Await.result((corpus ? CreateAnagrams(Set("read", "red", "dear"))).mapTo[Boolean], Duration(5, "seconds"))
      }
    }

    "Basic functionality of anagrams if they are valid anagrams" in {
      Stats.empty mustBe Stats(0, 0, 0, 0.0)
      val corpus = app.actorSystem.actorOf(AnagramCorpus.props(new Dictionary(app.configuration)))
      corpus ! CreateAnagrams(Set("read", "dare", "dear"))
      corpus ! CreateAnagrams(Set("teal", "tale"))
      corpus ! CreateAnagrams(Set("ate", "tea", "eat"))
      Await.result(corpus ? GetAnagrams("dare"), Duration(5, "seconds")) mustBe Set("read", "dear")
      Await.result(corpus ? GetAnagrams("teal"), Duration(5, "seconds")) mustBe Set("tale")
      Await.result(corpus ? GetAnagrams("dare", Some(4)), Duration(5, "seconds")) mustBe Set("read", "dear")
      Await.result(corpus ? GetAnagrams("dare", Some(1)), Duration(5, "seconds")) mustBe Set("read")

      //Check stats
      Await.result(corpus ? GetStats, Duration(5, "seconds")) mustBe Stats(3, 4, 4, 3.625)
      Await.result(corpus ? MaxAnagrams, Duration(5, "seconds")) mustBe List(Anagrams(Set("read", "dare", "dear")), Anagrams(Set("ate", "tea", "eat")))
      Await.result(corpus ? FilterBySize(3), Duration(5, "seconds")) mustBe List(Anagrams(Set("read", "dare", "dear")), Anagrams(Set("ate", "tea", "eat")))

      //Delete
      Await.result(corpus ? Delete("teal"), Duration(5, "seconds")) mustBe true
      Await.result(corpus ? GetAnagrams("teal"), Duration(5, "seconds")) mustBe Set.empty
      Await.result(corpus ? DeleteAnagrams("ate"), Duration(5, "seconds")) mustBe true
      Await.result(corpus ? GetAnagrams("ate"), Duration(5, "seconds")) mustBe Set.empty

      //Reset
      Await.result(corpus ? Reset, Duration(5, "seconds")) mustBe true
      Await.result(corpus ? GetAnagrams("dare", Some(1)), Duration(5, "seconds")) mustBe Set.empty
    }
  }

}
