# Scala implementation of Aho-Corasick algorithm

This is a simple implementation of Aho-Corasick algorithm written in purely scala, which is byte-oriented. The best feature is that it supports adding the property of word.

## Usage

```scala
// create AC automaton
val ac = new Automaton
// "user defined" is the property of words
ac.addWords("user defined", "he", "she", "his", "hers")
ac.setFailTransitions()

// search pattern string
val results = ac.search("ushers")
```


 
 
