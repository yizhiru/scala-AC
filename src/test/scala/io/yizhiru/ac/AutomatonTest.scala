package io.yizhiru.ac

import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AutomatonTest extends AnyFlatSpec with Matchers {

  "words: he, she, hers" should "be matched without an explicit build step" in {
    val ac = new Automaton
    ac.addWords("pronoun", "he", "she", "his", "hers")
    val matchedWords = ac.search("ushers").map(_._1)

    matchedWords should have size 3
    matchedWords should contain allOf("he", "she", "hers")
    matchedWords should not contain allOf("ss", "his", "sh")
  }

  "words: 微微 微微一笑 已赢回 赢回" should "match utf-8 chinese text" in {
    val ac = new Automaton
    ac.addWords("自定义词", "微微", "微微一笑", "已赢回", "赢回", "赢回帅", "主信")
    val matchedWords = ac
      .search("阿联只是微微一笑，他已赢回主帅信任")
      .map(_._1)

    matchedWords should have size 4
    matchedWords should contain allOf("微微", "微微一笑", "已赢回", "赢回")
    matchedWords should not contain allOf("赢回帅", "主信")
  }

  it should "return keyword properties" in {
    val ac = new Automaton
    ac.addWords("pronoun", "he", "she", "his", "hers")
    val matches = ac.search("ushers")

    matches should contain allOf(
      ("he", "pronoun"),
      ("she", "pronoun"),
      ("hers", "pronoun")
    )
  }

  it should "search raw bytes directly" in {
    val ac = new Automaton
    ac.addWordBytes("tag", Array[Byte]('h', 'e'))
    ac.searchBytes(Array[Byte]('u', 's', 'h', 'e', 'r', 's')) should contain(("he", "tag"))
  }

  it should "rebuild automatically after new keywords are added" in {
    val ac = new Automaton
    ac.addWord("tag", "he")
    ac.search("he") should contain(("he", "tag"))

    ac.addWord("tag", "she")
    ac.search("she") should contain allOf(("he", "tag"), ("she", "tag"))
  }

  it should "allow eager build" in {
    val ac = new Automaton
    ac.addWords("tag", "he", "she")
    ac.build()
    val statesBefore = ac.stateCount
    ac.build()
    ac.stateCount shouldBe statesBefore
  }

  it should "return no matches for empty text" in {
    val ac = new Automaton
    ac.addWord("tag", "he")
    ac.search("") shouldBe empty
    ac.searchBytes(Array.emptyByteArray) shouldBe empty
  }

  it should "return no matches when the vocabulary is empty" in {
    val ac = new Automaton
    ac.search("anything") shouldBe empty
  }

  "setFailTransitions" should "remain compatible as an alias for build" in {
    val ac = new Automaton
    ac.addWords("tag", "he", "she")
    ac.setFailTransitions()
    ac.search("she") should contain allOf(("he", "tag"), ("she", "tag"))
  }

  "a larger vocabulary" should "match deterministically" in {
    val ac = new Automaton
    val words = (0 until 200).map(i => s"token$i")
    ac.addWords("batch", words: _*)

    val haystack = words.mkString(" ")
    val matches = ac.search(haystack).map(_._1)

    matches should have size words.size
    words.foreach(word => matches should contain(word))
  }
}
