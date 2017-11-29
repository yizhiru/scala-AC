package io.yizhiru.ac

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Automaton {

    // initialization
    val trie: ArrayBuffer[Node] = ArrayBuffer(Node(0, 0.asInstanceOf[Byte],
        mutable.Map[Byte, Int](), 0, mutable.Set[(String, String)]()))

    // goto from node according to byte value
    private def gotoState(node: Node, byte: Byte): Int = {
        node.goto.getOrElse(byte, -1)
    }

    /**
      * add key word
      *
      * @param word key word
      * @param pro  word property
      */
    def addWord(word: String, pro: String): Unit = {
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
        trie(state).output += ((word, pro))
    }

    // add words
    def addWords(pro: String, words: String*): Unit = {
        words.foreach(t => addWord(t, pro))
    }

    // set failure function
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

    // search words
    def search(str: String) = {
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