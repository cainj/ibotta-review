package modules

import com.google.inject.AbstractModule
import models.AnagramCorpus
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AnagramModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[AnagramCorpus]("corpus-actor")
  }
}

