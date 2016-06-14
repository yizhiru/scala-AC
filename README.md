# scala--implementation-of-aho-corasick

This is a simple implementation of Aho-Corasick algorithm written in purely scala. The best feature is that it support adding the property of word.

## Usage

```scala
// create AC automaton
val ac = new Automaton
ac.addWords("ss", "he", "she", "his", "hers")
ac.setFailTransitions()

// search pattern string
val results = ac.search("ushers")
```


 
 