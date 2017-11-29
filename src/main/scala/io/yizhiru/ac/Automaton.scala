package io.yizhiru.ac

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Automaton {

    // initialization
    val trie: ArrayBuffer[Node] = ArrayBuffer(Node(0, 0.asInstanceOf[Byte],
        mutable.Map[Byte, Int](), 0, mutable.Set[(String, String)]()))

    /**
      * Goto a state according to byte value.
      *
      * @param node current node.
      * @param byte byte value.
      * @return destination node.
      */
    private def gotoState(node: Node, byte: Byte): Int = {
        node.goto.getOrElse(byte, -1)
    }

    /**
      * Add key word.
      *
      * @param property word property
      * @param word     key word
      */
    def addWord(property: String, word: String): Unit = {
        var state = 0
        word.getBytes().foreach { t =>
            gotoState(trie(state), t) match {
                case -1 => val next = trie.size
                    trie(state).goto += (t -> next)
                    trie += Node(next, t, mutable.Map[Byte, Int](), 0, mutable.Set[(String, String)]())
                    state = next
                case _ => state = gotoState(trie(state), t)
            }
        }
        trie(state).output += ((word, property))
    }

    /**
      * Add key words.
      *
      * @param property word property.
      * @param words    key words.
      */
    def addWords(property: String, words: String*): Unit = {
        words.foreach {
            word => addWord(property, word)
        }
    }

    /**
      * Set failure function.
      */
    def setFailTransitions(): Unit = {
        val queue = mutable.Queue[Int]()
        // set failure for node whose depth=1
        trie(0).goto.foreach { case (a, s) =>
            queue += s
            trie(s).failure = 0
        }
        while (queue.nonEmpty) {
            val r = queue.dequeue()
            trie(r).goto.foreach { case (a, s) =>
                queue += s
                var state = trie(r).failure
                while (gotoState(trie(state), a) == -1 && state != 0)
                    state = trie(state).failure
                val goto_a: Int = state == 0 && gotoState(trie(state), a) == -1 match {
                    case true => 0
                    case _ => trie(state).goto.getOrElse(a, 0)
                }
                trie(s).failure = goto_a
                trie(s).output ++= trie(trie(s).failure).output
            }
        }
    }

    /**
      * Search words.
      *
      * @param str string.
      * @return matched key words.
      */
    def search(str: String): mutable.Set[(String, String)] = {
        val resultSet = mutable.Set[(String, String)]()
        var node = trie(0)
        str.getBytes.foreach { a =>
            while (gotoState(node, a) == -1 && node.state != 0)
                node = trie(node.failure)
            node = node.state == 0 && gotoState(node, a) == -1 match {
                case true => trie(0)
                case _ => trie(gotoState(node, a))
            }
            if (node.output.nonEmpty)
                resultSet ++= node.output
        }
        resultSet
    }
}