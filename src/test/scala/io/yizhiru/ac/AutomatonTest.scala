package io.yizhiru.ac

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

class AutomatonTest extends FlatSpec with Matchers {

    "words: he, she, hers" should "be matched" in {
        val ac = new Automaton
        ac.addWords("pronoun", "he", "she", "his", "hers")
        ac.setFailTransitions()
        val matchedWords: mutable.Set[String] = ac
                .search("ushers")
                .map(t => t._1)

        matchedWords should have size 3L
        matchedWords should contain("he")
        matchedWords should contain("she")
        matchedWords should contain("hers")
        matchedWords shouldNot contain("ss")
        matchedWords shouldNot contain("his")
        matchedWords shouldNot contain("sh")
    }

    "words: 微微 微微一笑 已赢回 赢回" should "matched" in {
        val ac = new Automaton
        ac.addWords("自定义词", "微微", "微微一笑", "已赢回", "赢回", "赢回帅", "主信")
        ac.setFailTransitions()
        val matchedWords: mutable.Set[String] = ac
                .search("阿联只是微微一笑，他已赢回主帅信任")
                .map(t => t._1)

        matchedWords should have size 4L
        matchedWords should contain("微微")
        matchedWords should contain("微微一笑")
        matchedWords should contain("已赢回")
        matchedWords should contain("赢回")
        matchedWords shouldNot contain("赢回帅")
        matchedWords shouldNot contain("主信")
    }
}