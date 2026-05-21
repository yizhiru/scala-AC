package io.yizhiru.ac

import java.nio.charset.StandardCharsets

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Byte-oriented Aho-Corasick automaton with UTF-8 string helpers.
  *
  * Failure links are built automatically on the first [[search]] or [[searchBytes]] call.
  * Call [[build]] eagerly when you want to pay construction cost up front.
  */
final class Automaton {

  private val charset = StandardCharsets.UTF_8
  private val vocabulary = ArrayBuffer.empty[(String, String)]
  private val trie = ArrayBuffer(Node.create())
  private val directGoto = ArrayBuffer(Array.fill(Node.AlphabetSize)(Node.NoTransition))
  private val directOutputs = ArrayBuffer(mutable.Set.empty[Int])
  private var built = false

  /** UTF-8 charset used for [[addWord]] and [[search]]. */
  def encoding: java.nio.charset.Charset = charset

  /**
    * Add a keyword with an associated property tag.
    *
    * @param property metadata attached to the keyword
    * @param word     keyword text, encoded as UTF-8
    */
  def addWord(property: String, word: String): Unit =
    addWordBytes(property, word.getBytes(charset))

  /**
    * Add multiple keywords that share the same property tag.
    */
  def addWords(property: String, words: String*): Unit =
    words.foreach(addWord(property, _))

  /**
    * Add a keyword from raw bytes without charset conversion.
    */
  def addWordBytes(property: String, word: Array[Byte]): Unit = {
    invalidate()
    var state = 0
    word.foreach { byte =>
      val index = byteToIndex(byte)
      directGoto(state)(index) match {
        case Node.NoTransition =>
          val nextState = allocateState()
          directGoto(state)(index) = nextState
          state = nextState
        case next =>
          state = next
      }
    }
    val entry = (new String(word, charset), property)
    val vocabularyIndex =
      vocabulary.indexOf(entry) match {
        case -1 =>
          vocabulary += entry
          vocabulary.size - 1
        case idx => idx
      }
    directOutputs(state) += vocabularyIndex
  }

  /** Build failure links and complete the goto table. Safe to call multiple times. */
  def build(): Unit = {
    if (built) return
    restoreDirectStructure()
    buildFailureLinks()
    completeGotoTable()
    built = true
  }

  /**
    * @deprecated Failure links are built automatically; use [[build]] for eager construction.
    */
  @deprecated("Use build() instead; failure links are built automatically before search", "1.1")
  def setFailTransitions(): Unit = build()

  /**
    * Search UTF-8 text and return matched `(keyword, property)` pairs.
    */
  def search(text: String): Set[(String, String)] =
    searchBytes(text.getBytes(charset))

  /**
    * Search raw bytes without charset conversion.
    */
  def searchBytes(data: Array[Byte]): Set[(String, String)] = {
    build()
    val matches = mutable.Set.empty[(String, String)]
    var state = 0
    data.foreach { byte =>
      state = trie(state).goto(byteToIndex(byte))
      trie(state).output.foreach(idx => matches += vocabulary(idx))
    }
    matches.toSet
  }

  /** Number of trie states after the last build. */
  def stateCount: Int = {
    build()
    trie.size
  }

  /** Number of registered keywords (including duplicates with different properties). */
  def vocabularySize: Int = vocabulary.size

  private def invalidate(): Unit = built = false

  private def byteToIndex(byte: Byte): Int = byte & 0xff

  private def allocateState(): Int = {
    trie += Node.create()
    directGoto += Array.fill(Node.AlphabetSize)(Node.NoTransition)
    directOutputs += mutable.Set.empty[Int]
    trie.size - 1
  }

  /** Reset mutable build artifacts from the sparse trie edges. */
  private def restoreDirectStructure(): Unit = {
    var state = 0
    while (state < trie.size) {
      System.arraycopy(directGoto(state), 0, trie(state).goto, 0, Node.AlphabetSize)
      trie(state).failure = 0
      trie(state).output.clear()
      trie(state).output ++= directOutputs(state)
      state += 1
    }
  }

  private def buildFailureLinks(): Unit = {
    val queue = mutable.Queue.empty[Int]
    val root = trie(0)

    var index = 0
    while (index < Node.AlphabetSize) {
      val child = root.goto(index)
      if (child != Node.NoTransition) {
        trie(child).failure = 0
        queue.enqueue(child)
      }
      index += 1
    }

    while (queue.nonEmpty) {
      val current = queue.dequeue()
      var byteIndex = 0
      while (byteIndex < Node.AlphabetSize) {
        val child = trie(current).goto(byteIndex)
        if (child != Node.NoTransition) {
          queue.enqueue(child)
          var failState = trie(current).failure
          while (failState != 0 && trie(failState).goto(byteIndex) == Node.NoTransition) {
            failState = trie(failState).failure
          }
          val failureTarget =
            if (failState == 0 && trie(failState).goto(byteIndex) == Node.NoTransition) 0
            else trie(failState).goto(byteIndex)
          trie(child).failure = failureTarget
          trie(child).output ++= trie(failureTarget).output
        }
        byteIndex += 1
      }
    }
  }

  private def completeGotoTable(): Unit = {
    val queue = mutable.Queue.empty[Int]

    var index = 0
    while (index < Node.AlphabetSize) {
      if (trie(0).goto(index) == Node.NoTransition) {
        trie(0).goto(index) = 0
      } else {
        queue.enqueue(trie(0).goto(index))
      }
      index += 1
    }

    while (queue.nonEmpty) {
      val current = queue.dequeue()
      var byteIndex = 0
      while (byteIndex < Node.AlphabetSize) {
        val child = trie(current).goto(byteIndex)
        if (child != Node.NoTransition) {
          queue.enqueue(child)
        } else {
          trie(current).goto(byteIndex) = trie(trie(current).failure).goto(byteIndex)
        }
        byteIndex += 1
      }
    }
  }
}
