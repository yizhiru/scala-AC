package io.yizhiru.ac

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class AutomatonTest extends FlatSpec with Matchers {

    "words: he, she, hers" should "match string `ushers`" in {
        val ac = new Automaton
        ac.addWords("ss", "he", "she", "his", "hers")
        ac.setFailTransitions()
        val matchedWords: mutable.Set[String] = ac.search("ushers")
                .map(t => t._1)

        matchedWords shouldNot contain("ss")
        matchedWords should contain("he")
        matchedWords should contain("she")
        matchedWords shouldNot contain("his")
        matchedWords should contain("hers")
        matchedWords shouldNot contain("sh")
    }
}