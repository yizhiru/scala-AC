package io.yizhiru.ac

import scala.collection.mutable

/**
  * Trie node in an Aho-Corasick automaton.
  *
  * @param failure failure link state index
  * @param goto    transition table indexed by unsigned byte (0-255), -1 before build completes
  * @param output  vocabulary indices for pattern ends reachable from this state
  */
private[ac] final class Node(
    var failure: Int,
    val goto: Array[Int],
    val output: mutable.Set[Int]
)

private[ac] object Node {

  private[ac] val AlphabetSize = 256
  private[ac] val NoTransition = -1

  private[ac] def create(): Node =
    new Node(
      failure = 0,
      goto = Array.fill(AlphabetSize)(NoTransition),
      output = mutable.Set.empty
    )
}
